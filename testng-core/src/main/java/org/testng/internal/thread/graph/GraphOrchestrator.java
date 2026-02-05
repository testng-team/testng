package org.testng.internal.thread.graph;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import org.testng.IDynamicGraph;
import org.testng.collections.Maps;
import org.testng.internal.AutoCloseableLock;
import org.testng.internal.RuntimeBehavior;
import org.testng.log4testng.Logger;
import org.testng.thread.IThreadWorkerFactory;
import org.testng.thread.IWorker;

/**
 * An orchestrator that works with a {@link IDynamicGraph} graph to execute nodes from the DAG in an
 * concurrent fashion by using a {@link ThreadPoolExecutor}
 */
public class GraphOrchestrator<T> {
  private final ExecutorService service;
  private final IDynamicGraph<T> graph;
  private final Map<T, IWorker<T>> mapping = Maps.newConcurrentMap();
  private final Map<T, T> upstream = Maps.newConcurrentMap();
  private final Comparator<T> comparator;
  private final IThreadWorkerFactory<T> factory;

  private final AutoCloseableLock internalLock = new AutoCloseableLock();

  public GraphOrchestrator(
      ExecutorService service,
      IThreadWorkerFactory<T> factory,
      IDynamicGraph<T> graph,
      Comparator<T> comparator) {
    this.service = service;
    this.graph = graph;
    this.comparator = comparator;
    this.factory = factory;
  }

  public void run() {
    try (AutoCloseableLock ignore = internalLock.lock()) {
      List<T> freeNodes = graph.getFreeNodes();
      if (comparator != null) {
        freeNodes.sort(comparator);
      }
      runNodes(freeNodes);
    }
  }

  private void runNodes(List<T> freeNodes) {
    List<IWorker<T>> workers = factory.createWorkers(freeNodes);
    mapNodeToWorker(workers, freeNodes);

    for (IWorker<T> worker : workers) {
      mapNodeToParent(freeNodes);
      setStatus(worker, IDynamicGraph.Status.RUNNING);
      try {
        TestNGFutureTask<T> task = new TestNGFutureTask<>(worker, this::afterExecute);
        service.execute(task);
      } catch (Exception ex) {
        Logger.getLogger(GraphOrchestrator.class).error(ex.getMessage(), ex);
      }
    }
  }

  private void mapNodeToParent(List<T> freeNodes) {
    if (!RuntimeBehavior.enforceThreadAffinity()) {
      return;
    }
    for (T freeNode : freeNodes) {
      List<T> nodes = graph.getDependenciesFor(freeNode);
      nodes.forEach(eachNode -> upstream.put(eachNode, freeNode));
    }
  }

  private void afterExecute(IWorker<T> original, IWorker<T> result) {
    IWorker<T> r = Optional.ofNullable(result).orElse(original);
    try (AutoCloseableLock ignore = internalLock.lock()) {
      setStatus(r, computeStatus(r));
      if (graph.getNodeCount() == graph.getNodeCountWithStatus(IDynamicGraph.Status.FINISHED)) {
        service.shutdown();
      } else {
        List<T> freeNodes = graph.getFreeNodes();
        if (comparator != null) {
          freeNodes.sort(comparator);
        }
        handleThreadAffinity(freeNodes);
        runNodes(freeNodes);
      }
    }
  }

  private void handleThreadAffinity(List<T> freeNodes) {
    if (!RuntimeBehavior.enforceThreadAffinity()) {
      return;
    }
    for (T node : freeNodes) {
      T upstreamNode = upstream.get(node);
      if (upstreamNode == null) {
        continue;
      }
      IWorker<T> w = mapping.get(upstreamNode);
      if (w != null) {
        long threadId = w.getCurrentThreadId();
        mapping.put(node, new PhoneyWorker<>(threadId));
      }
    }
  }

  private IDynamicGraph.Status computeStatus(IWorker<T> worker) {
    IDynamicGraph.Status status = IDynamicGraph.Status.FINISHED;
    if (RuntimeBehavior.enforceThreadAffinity() && !worker.completed()) {
      status = IDynamicGraph.Status.READY;
    }
    return status;
  }

  private void setStatus(IWorker<T> worker, IDynamicGraph.Status status) {
    try (AutoCloseableLock ignore = internalLock.lock()) {
      for (T m : worker.getTasks()) {
        graph.setStatus(m, status);
      }
    }
  }

  private void mapNodeToWorker(List<IWorker<T>> runnables, List<T> freeNodes) {
    if (!RuntimeBehavior.enforceThreadAffinity()) {
      return;
    }
    for (IWorker<T> runnable : runnables) {
      for (T freeNode : freeNodes) {
        IWorker<T> w = mapping.get(freeNode);
        if (w != null) {
          long current = w.getThreadIdToRunOn();
          runnable.setThreadIdToRunOn(current);
        }
        if (runnable.toString().contains(freeNode.toString())) {
          mapping.put(freeNode, runnable);
        }
      }
    }
  }
}
