package org.testng.internal.thread.graph;

import org.testng.TestNGException;
import org.testng.collections.Maps;
import org.testng.internal.DynamicGraph;
import org.testng.internal.DynamicGraph.Status;
import org.testng.internal.RuntimeBehavior;
import org.testng.internal.thread.TestNGThreadFactory;
import org.testng.log4testng.Logger;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An Executor that launches tasks per batches. It takes a {@code DynamicGraph} of tasks to be run
 * and a {@code IThreadWorkerFactory} to initialize/create {@code Runnable} wrappers around those
 * tasks
 */
public class GraphThreadPoolExecutor<T> extends ThreadPoolExecutor {

  private final DynamicGraph<T> m_graph;
  private final IThreadWorkerFactory<T> m_factory;
  private final Map<T, IWorker<T>> mapping = Maps.newConcurrentMap();
  private final Map<T, T> upstream = Maps.newConcurrentMap();
  private final Comparator<T> m_comparator;

  public GraphThreadPoolExecutor(
      String name,
      DynamicGraph<T> graph,
      IThreadWorkerFactory<T> factory,
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      Comparator<T> comparator) {
    super(
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        new TestNGThreadFactory(name));
    m_graph = graph;
    m_factory = factory;
    m_comparator = comparator;

    if (m_graph.getFreeNodes().isEmpty()) {
      throw new TestNGException("The graph of methods contains a cycle:" + graph);
    }
  }

  public void run() {
    synchronized (m_graph) {
      List<T> freeNodes = m_graph.getFreeNodes();
      if (m_comparator != null) {
          Collections.sort(freeNodes, m_comparator);
      }
      runNodes(freeNodes);
    }
  }

  /** Create one worker per node and execute them. */
  private void runNodes(List<T> freeNodes) {
    List<IWorker<T>> workers = m_factory.createWorkers(freeNodes);
    mapNodeToWorker(workers, freeNodes);

    for (int ix = 0; ix < workers.size(); ix++) {
      IWorker<T> worker = workers.get(ix);
      mapNodeToParent(freeNodes, ix);
      setStatus(worker, Status.RUNNING);
      try {
        execute(worker);
      } catch (Exception ex) {
        Logger.getLogger(GraphThreadPoolExecutor.class).error(ex.getMessage(), ex);
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void afterExecute(Runnable r, Throwable t) {
    synchronized (m_graph) {
      setStatus((IWorker<T>) r, computeStatus(r));
      if (m_graph.getNodeCount() == m_graph.getNodeCountWithStatus(Status.FINISHED)) {
        shutdown();
      } else {
        List<T> freeNodes = m_graph.getFreeNodes();
        if (m_comparator != null) {
            Collections.sort(freeNodes, m_comparator);
        }
        handleThreadAffinity(freeNodes);
        runNodes(freeNodes);
      }
    }
  }

  private void setStatus(IWorker<T> worker, Status status) {
    synchronized (m_graph) {
      for (T m : worker.getTasks()) {
        m_graph.setStatus(m, status);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Status computeStatus(Runnable r) {
    IWorker<T> worker = (IWorker<T>) r;
    Status status = Status.FINISHED;
    if (RuntimeBehavior.enforceThreadAffinity() && !worker.completed()) {
      status = Status.READY;
    }
    return status;
  }

  private void mapNodeToWorker(List<IWorker<T>> runnables, List<T> freeNodes) {
    if (!RuntimeBehavior.enforceThreadAffinity()) {
      return;
    }
    int index = 0;
    for (IWorker<T> runnable : runnables) {
      T freeNode = freeNodes.get(index++);
      IWorker<T> w = mapping.get(freeNode);
      if (w != null) {
        long current = w.getThreadIdToRunOn();
        runnable.setThreadIdToRunOn(current);
      }
      mapping.put(freeNode, runnable);
    }
  }

  private void mapNodeToParent(List<T> freeNodes, int ix) {
    if (!RuntimeBehavior.enforceThreadAffinity()) {
      return;
    }
    T key = freeNodes.get(ix);
    List<T> nodes = m_graph.getDependenciesFor(key);
    nodes.forEach(eachNode -> upstream.put(eachNode, key));
  }

  private void handleThreadAffinity(List<T> freeNodes) {
    if (!RuntimeBehavior.enforceThreadAffinity()) {
      return;
    }
    for (T node : freeNodes) {
      IWorker<T> w = mapping.get(upstream.get(node));
      long threadId = w.getCurrentThreadId();
      mapping.put(node, new PhoneyWorker(threadId));
    }
  }

  private class PhoneyWorker implements IWorker<T> {
    private long threadId;

    public PhoneyWorker(long threadId) {
      this.threadId = threadId;
    }

    @Override
    public List<T> getTasks() {
      return null;
    }

    @Override
    public long getTimeOut() {
      return 0;
    }

    @Override
    public int getPriority() {
      return 0;
    }

    @Override
    public int compareTo(@Nonnull IWorker<T> o) {
      return 0;
    }

    @Override
    public void run() {}

    @Override
    public long getThreadIdToRunOn() {
      return threadId;
    }
  }
}
