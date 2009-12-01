package org.testng.internal.thread;

import org.testng.ITestNGMethod;
import org.testng.internal.DynamicGraph;
import org.testng.internal.IMethodWorker;
import org.testng.internal.IWorkerFactory;
import org.testng.internal.DynamicGraph.Status;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An Executor that launches tasks per batches.
 */
public class GroupThreadPoolExecutor extends ThreadPoolExecutor {
  private static final boolean DEBUG = false;

  private DynamicGraph<ITestNGMethod> m_graph;
  private List<Runnable> m_activeRunnables = new ArrayList<Runnable>();
  private IWorkerFactory m_factory;
  private XmlTest m_xmlTest;

  public GroupThreadPoolExecutor(IWorkerFactory factory, XmlTest xmlTest, int corePoolSize,
      int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
      DynamicGraph<ITestNGMethod> graph) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    m_graph = graph;
    m_factory = factory;
    m_xmlTest = xmlTest;
  }

  public void run() {
    synchronized(m_graph) {
      Set<ITestNGMethod> freeNodes = m_graph.getFreeNodes();
      runNodes(freeNodes);
    }
  }

  /**
   * Create one worker per node and execute them.
   */
  private void runNodes(Set<ITestNGMethod> nodes) {
    List<IMethodWorker> runnables = m_factory.createWorkers(m_xmlTest, nodes);
    for (IMethodWorker r : runnables) {
      m_activeRunnables.add(r);
      setStatus(r, Status.RUNNING);
      ppp("Executing: " + r);
      try {
        execute(r);
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private void setStatus(IMethodWorker worker, Status status) {
    ppp("Set status:" + worker + " status:" + status);
    synchronized(m_graph) {
      for (ITestNGMethod m : worker.getMethods()) {
        m_graph.setStatus(m, status);
      }
    }
  }

  @Override
  public void afterExecute(Runnable r, Throwable t) {
    ppp("Finished:" + r);
    m_activeRunnables.remove(r);
    setStatus((IMethodWorker) r, Status.FINISHED);
    synchronized(m_graph) {
      ppp("NODE COUNT:" + m_graph.getNodeCount() + " and "
          + m_graph.getNodeCountWithStatus(Status.FINISHED));
      if (m_graph.getNodeCount() == m_graph.getNodeCountWithStatus(Status.FINISHED)) {
        ppp("SHUTTING DOWN EXECUTOR " + this);
        shutdown();
      } else {
        Set<ITestNGMethod> freeNodes = m_graph.getFreeNodes();
        runNodes(freeNodes);
      }
    }
//    if (m_activeRunnables.isEmpty() && m_index < m_runnables.getSize()) {
//      runNodes(m_index++);
//    }
  }

  private void ppp(String string) {
    if (DEBUG) {
      System.out.println("   [GroupThreadPoolExecutor] " + Thread.currentThread().getId() + " "
          + string);
    }
  }

  // public void addRunnable(int i, Runnable runnable) {
  // List<TestMethodWorker> l = m_runnables.get(i);
  // if (l == null) {
  // l = new ArrayList<TestMethodWorker>();
  // m_runnables.put(i, l);
  // }
  // l.add(runnable);
  // }
}
