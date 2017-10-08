package org.testng.internal.thread.graph;

import org.testng.TestNGException;
import org.testng.internal.DynamicGraph;
import org.testng.internal.DynamicGraph.Status;
import org.testng.internal.thread.TestNGThreadFactory;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An Executor that launches tasks per batches. It takes a {@code DynamicGraph}
 * of tasks to be run and a {@code IThreadWorkerFactory} to initialize/create
 * {@code Runnable} wrappers around those tasks
 */
public class GraphThreadPoolExecutor<T> extends ThreadPoolExecutor {

  private final DynamicGraph<T> m_graph;
  private final Queue<Runnable> m_activeRunnables = new ConcurrentLinkedDeque<>();
  private final IThreadWorkerFactory<T> m_factory;

  public GraphThreadPoolExecutor(String name, DynamicGraph<T> graph, IThreadWorkerFactory<T> factory, int corePoolSize,
      int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new TestNGThreadFactory(name));
    m_graph = graph;
    m_factory = factory;

    if (m_graph.getFreeNodes().isEmpty()) {
      throw new TestNGException("The graph of methods contains a cycle:" + graph.getEdges());
    }
  }

  public void run() {
    synchronized(m_graph) {
      List<T> freeNodes = m_graph.getFreeNodes();
      runNodes(freeNodes);
    }
  }

  /**
   * Create one worker per node and execute them.
   */
  private void runNodes(List<T> freeNodes) {
    List<IWorker<T>> runnables = m_factory.createWorkers(freeNodes);
    for (IWorker<T> r : runnables) {
      m_activeRunnables.add(r);
      setStatus(r, Status.RUNNING);
      try {
        execute(r);
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private void setStatus(IWorker<T> worker, Status status) {
    if (status == Status.FINISHED) {
      m_activeRunnables.remove(worker);
    }
    synchronized(m_graph) {
      for (T m : worker.getTasks()) {
        m_graph.setStatus(m, status);
      }
    }
  }

  @Override
  public void afterExecute(Runnable r, Throwable t) {
    setStatus((IWorker<T>) r, Status.FINISHED);
    synchronized(m_graph) {
      if (m_graph.getNodeCount() == m_graph.getNodeCountWithStatus(Status.FINISHED)) {
        shutdown();
      } else {
        List<T> freeNodes = m_graph.getFreeNodes();
        runNodes(freeNodes);
      }
    }
  }
}
