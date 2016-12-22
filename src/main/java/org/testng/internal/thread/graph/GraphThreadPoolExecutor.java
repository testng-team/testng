package org.testng.internal.thread.graph;

import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.internal.DynamicGraph;
import org.testng.internal.DynamicGraph.Status;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An Executor that launches tasks per batches. It takes a {@code DynamicGraph}
 * of tasks to be run and a {@code IThreadWorkerFactory} to initialize/create
 * {@code Runnable} wrappers around those tasks
 */
public class GraphThreadPoolExecutor<T> extends ThreadPoolExecutor {
  private static final boolean DEBUG = false;
  /** Set to true if you want to generate GraphViz graphs */
  private static final boolean DOT_FILES = false;

  private DynamicGraph<T> graph;
  private List<Runnable> activeRunnables = Lists.newArrayList();
  private IThreadWorkerFactory<T> factory;
  private List<String> dotFiles = Lists.newArrayList();
  private int threadCount;

  public GraphThreadPoolExecutor(DynamicGraph<T> graph, IThreadWorkerFactory<T> factory, int corePoolSize,
      int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue
        /* , new TestNGThreadPoolFactory() */);
    ppp("Initializing executor with " + corePoolSize + " threads and following graph " + graph);
    threadCount = maximumPoolSize;
    this.graph = graph;
    this.factory = factory;

    if (this.graph.getFreeNodes().isEmpty()) {
      throw new TestNGException("The graph of methods contains a cycle:" + graph.getEdges());
    }
  }

  public void run() {
    synchronized(graph) {
      if (DOT_FILES) {
        dotFiles.add(graph.toDot());
      }
      List<T> freeNodes = graph.getFreeNodes();
      runNodes(freeNodes);
    }
  }

  /**
   * Create one worker per node and execute them.
   */
  private void runNodes(List<T> freeNodes) {
    List<IWorker<T>> runnables = factory.createWorkers(freeNodes);
    for (IWorker<T> r : runnables) {
      activeRunnables.add(r);
      ppp("Added to active runnable");
      setStatus(r, Status.RUNNING);
      ppp("Executing: " + r);
      try {
        execute(r);
//        if (threadCount > 1) execute(r);
//        else r.run();
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private void setStatus(IWorker<T> worker, Status status) {
    ppp("Set status:" + worker + " status:" + status);
    if (status == Status.FINISHED) {
      activeRunnables.remove(worker);
    }
    synchronized(graph) {
      for (T m : worker.getTasks()) {
        graph.setStatus(m, status);
      }
    }
  }

  @Override
  public void afterExecute(Runnable r, Throwable t) {
    ppp("Finished runnable:" + r);
    setStatus((IWorker<T>) r, Status.FINISHED);
    synchronized(graph) {
      ppp("Node count:" + graph.getNodeCount() + " and "
          + graph.getNodeCountWithStatus(Status.FINISHED) + " finished");
      if (graph.getNodeCount() == graph.getNodeCountWithStatus(Status.FINISHED)) {
        ppp("Shutting down executor " + this);
        if (DOT_FILES) {
          generateFiles(dotFiles);
        }
        shutdown();
      } else {
        if (DOT_FILES) {
          dotFiles.add(graph.toDot());
        }
        List<T> freeNodes = graph.getFreeNodes();
        runNodes(freeNodes);
      }
    }
//    if (activeRunnables.isEmpty() && m_index < m_runnables.getSize()) {
//      runNodes(m_index++);
//    }
  }

  private void generateFiles(List<String> files) {
    try {
      File dir = File.createTempFile("TestNG-", "");
      dir.delete();
      dir.mkdir();
      for (int i = 0; i < files.size(); i++) {
        File f = new File(dir, "" + (i < 10 ? "0" : "") + i + ".dot");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
          bw.append(files.get(i));
        }
      }
      if (DOT_FILES) {
        System.out.println("Created graph files in " + dir);
      }
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }

  private void ppp(String string) {
    if (DEBUG) {
      System.out.println("============ [GraphThreadPoolExecutor] " + Thread.currentThread().getId() + " "
          + string);
    }
  }

}

class TestNGThreadPoolFactory implements ThreadFactory {
  private int count = 0;

  @Override
  public Thread newThread(Runnable r) {
    Thread result = new Thread(r);
    result.setName("TestNG-" + count++);
    return result;
  }
}
