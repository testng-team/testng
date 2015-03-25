package org.testng.internal.thread.graph;

import org.testng.TestNGException;
import org.testng.TestRunner;
import org.testng.collections.Lists;
import org.testng.internal.DynamicGraph;
import org.testng.internal.DynamicGraph.Status;
import org.testng.internal.Utils;

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
  /**
   * Set suite verbosity level to at least VERBOSE_LEVEL_FOR_DEBUG (8) if you want to generate GraphViz graphs
   */
  private static final int VERBOSE_LEVEL_FOR_DEBUG = 8;

  private final DynamicGraph<T> m_graph;
  private final IThreadWorkerFactory<T> m_factory;
  private final List<String> m_dotFiles = Lists.newArrayList();

  public GraphThreadPoolExecutor(DynamicGraph<T> graph, IThreadWorkerFactory<T> factory, int corePoolSize,
      int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new TestNGThreadPoolFactory());
    debug("Initializing executor with " + corePoolSize + " threads and following graph " + graph);
    m_graph = graph;
    m_factory = factory;

    if (isDebug()) {
      try {
        final File file = File.createTempFile("TestNG-Graph-Before-Run", ".dot");
        final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.append(m_graph.toDot());
        bw.close();
        debug("Created before run graph file: " + file);
      } catch (IOException ex) {
        ex.printStackTrace();
      } catch (SecurityException ex) {
        ex.printStackTrace();
      }
    }

    if (m_graph.getFreeNodes().isEmpty()) {
      final StringBuilder sb = new StringBuilder("The methods dependency graph contains a cycle.");
      if (!isDebug()) {
        sb.append("\nSet verbose level to at least ").append(VERBOSE_LEVEL_FOR_DEBUG).append(" to generate .dot file for graph");
        sb.append("\nGraph:").append(graph.getEdges());
      } else {
        sb.append(" See graph .dot file path above");
      }
      throw new TestNGException(sb.toString());
    }
  }

  public void run() {
    checkAndScheduleNext();
  }

  /**
   * Create one worker per node and execute them.
   */
  private void runNodes(List<T> freeNodes) {
    List<IWorker<T>> runnables = m_factory.createWorkers(freeNodes);
    for (IWorker<T> r : runnables) {
      debug("Added to active runnable");
      setStatus(r, Status.RUNNING);
      debug("Executing: " + r);
      try {
        execute(r);
      }
      catch(Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private void setStatus(IWorker<T> worker, Status status) {
    debug("Set status:" + worker + " status:" + status);
    synchronized(m_graph) {
      for (T m : worker.getTasks()) {
        m_graph.setStatus(m, status);
      }
    }
  }

  @Override
  public void afterExecute(Runnable r, Throwable t) {
    debug("Finished runnable:" + r);
    //noinspection unchecked
    setStatus((IWorker<T>) r, Status.FINISHED);
    checkAndScheduleNext();
  }

  private void checkAndScheduleNext() {
    synchronized (m_graph) {
      final int total = m_graph.getNodeCount();
      final int finished = m_graph.getNodeCountWithStatus(Status.FINISHED);
      final int running = m_graph.getNodeCountWithStatus(Status.RUNNING);

      debug("Nodes total: " + total + "; finished: " + finished + "; running: " + running);
      if (total == finished) {
        debug("All tasks finished. Shutting down executor " + this);
        shutdown();
      } else if (total == finished + running) {
        debug("All tasks finished or running.");
        // Last worker will shutdown executor, not now.
      } else {
        // Some nodes are ready to start or cycle exists
        // Lets save graph state
        if (isDebug()) {
          m_dotFiles.add(m_graph.toDot());
        }
        final List<T> freeNodes = m_graph.getFreeNodes();
        debug("Free nodes: " + freeNodes.size());
        if (running == 0 && freeNodes.isEmpty()) {
          shutdown();
          throw new IllegalStateException("The graph of methods contains a cycle: " + m_graph.getEdges());
        }
        runNodes(freeNodes);
      }
    }
  }

  @Override
  public void shutdown() {
    if (isDebug() && !isShutdown()) {
      generateFiles(m_dotFiles);
    }
    super.shutdown();
  }

  private void generateFiles(List<String> files) {
    try {
      File dir = File.createTempFile("TestNG-", "");
      dir.delete();
      dir.mkdir();
      for (int i = 0; i < files.size(); i++) {
        File f = new File(dir, "" + (i < 10 ? "0" : "") + i + ".dot");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.append(files.get(i));
        bw.close();
      }
      if (isDebug()) {
        debug("Created graph files in " + dir);
      }
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }

  private void debug(String string) {
    if (isDebug()) {
      Utils.log("GraphThreadPoolExecutor", VERBOSE_LEVEL_FOR_DEBUG, Thread.currentThread().getId() + " " + string);
    }
  }

  boolean isDebug() {
    return TestRunner.getVerbose() >= VERBOSE_LEVEL_FOR_DEBUG;
  }

}

class TestNGThreadPoolFactory implements ThreadFactory {
  private int m_count = 0;

  @Override
  public Thread newThread(Runnable r) {
    Thread result = new Thread(r);
    // using 'TestNG' may have caveats here because of ThreadUtil#isTestNGThread()
    // inside MethodInvocationHelper#invokeWithTimeout()
    result.setName("TestNG-Worker-" + m_count++);
    return result;
  }
}
