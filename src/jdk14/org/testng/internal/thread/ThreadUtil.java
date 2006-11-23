package org.testng.internal.thread;


import java.util.List;

import org.testng.internal.Utils;
import org.testng.internal.thread.port.AtomicIntegerAdapter;

import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;
import edu.emory.mathcs.backport.java.util.concurrent.ExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.Executors;
import edu.emory.mathcs.backport.java.util.concurrent.RejectedExecutionException;

/**
 * Utility class to level up threading according to JDK 1.4
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class ThreadUtil {
  /**
   * @param workers
   * @param threadCount
   * @param maxTimeOut
   * @param b
   */
  public static void execute(List<? extends Runnable> workers, int threadCount, long maxTimeOut, boolean triggerAtOnce) {
    final CountDownLatch startGate= new CountDownLatch(1);
    final CountDownLatch endGate= new CountDownLatch(workers.size());
    final CountDownLatch usedStartGate= triggerAtOnce ? startGate : null;
    
    ExecutorService pooledExecutor= Executors.newFixedThreadPool(threadCount);
    for(final Runnable task: workers) {
      try {
        pooledExecutor.execute(new CountDownLatchedRunnable(task, endGate, usedStartGate));
      }
      catch(RejectedExecutionException reex) {
        ; // this should never happen as we submit all tasks at once
      }
    }

    try {
      if(triggerAtOnce) {
        usedStartGate.countDown();
      }
      endGate.await();
      pooledExecutor.shutdown();
    }
    catch(InterruptedException e) {
      Thread.currentThread().interrupt();
      log(2, "Error waiting for concurrent executors to finish " + e.getMessage());
    }
  }

  /*public static final void execute(List tasks, int threadPoolSize, long defaultTimeout) {
    IPooledExecutor executor= ThreadUtil.createPooledExecutor(threadPoolSize);

    for(int i= 0; i < tasks.size(); i++) {
      Runnable task= (Runnable) tasks.get(i);
      executor.execute(task);
    }

    try {
      executor.shutdown();
      log(3, "Waiting for termination, timeout:" + defaultTimeout);
      executor.awaitTermination(defaultTimeout);
      if(executor.isTerminated()) {
        log(3, "Timeout period passed, successful termination");
      }
      else {
        log(3, "Timeout period passed, execution not finished, waiting complete termination");
        while(!executor.isTerminated()) {
          executor.awaitTermination(100L);
        }
        log(3, "Successful complete termination");
      }
    }
    catch(InterruptedException e) {
      Utils.log("ThreadUtil:" + ThreadUtil.currentThreadInfo(), 3, "Error shutting down PooledExecutor " + e.getMessage());
    }
  }*/

  public static final String currentThreadInfo() {
    Thread currentThread= Thread.currentThread();
    return String.valueOf(currentThread.getName() + "@" + currentThread.hashCode());
  }

  public static final IExecutor createExecutor(int threadCount, String threadFactoryName) {
    return new org.testng.internal.thread.port.ExecutorAdapter(threadCount, createFactory(threadFactoryName));
  }

  public static final IAtomicInteger createAtomicInteger(int initialValue) {
    return new AtomicIntegerAdapter(initialValue);
  }
  
  private static void log(int level, String msg) {
    Utils.log("ThreadUtil:" + ThreadUtil.currentThreadInfo(), level, msg);
  }

  /*private static final ICountDown createCountDown(int count) {
    return new org.testng.internal.thread.port.CountDownAdapter(count);
  }*/

  /*public static final IPooledExecutor createPooledExecutor(int size) {
    return new org.testng.internal.thread.port.PooledExecutorAdapter(size);
  }*/

  private static final IThreadFactory createFactory(String name) {
    return new ThreadFactoryImpl(name);
  }

  public static class ThreadFactoryImpl implements IThreadFactory,
    edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory {
    private String m_methodName;

    public ThreadFactoryImpl(String name) {
      m_methodName= name;
    }

    public Thread newThread(Runnable run) {
      return new TestNGThread(run, m_methodName);
    }

    public Object getThreadFactory() {
      return this;
    }
  }

  /**
   * A special <code>Runnable</code> that uses <code>CountDownLatch</code>-s to
   * sync on start and to ackowledge its finish.
   */
  private static class CountDownLatchedRunnable implements Runnable {
    private final Runnable m_task;
    private final CountDownLatch m_startGate;
    private final CountDownLatch m_endGate;
    
    public CountDownLatchedRunnable(Runnable task, CountDownLatch endGate) {
      this(task, endGate, null);
    }
    
    public CountDownLatchedRunnable(Runnable task, CountDownLatch endGate, CountDownLatch startGate) {
      m_task= task;
      m_startGate= startGate;
      m_endGate= endGate;
    }

    public void run() {
      if(null != m_startGate) {
        try {
          m_startGate.await();
        }
        catch(InterruptedException iex) {
          log(2, "Cannot wait for startup gate when executing " + m_task + "; thread was already interrupted " + iex.getMessage());
          Thread.currentThread().interrupt();
          return;
        }
      }
      
      try {
        m_task.run();
      }
      finally {
        m_endGate.countDown();
      }
    }
  }
}
