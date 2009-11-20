package org.testng.internal.thread;


import org.testng.internal.MapList;
import org.testng.internal.TestMethodWorker;
import org.testng.internal.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


/**
 * A helper class to interface TestNG concurrency usage.
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>Alex Popescu</a>
 */
public class ThreadUtil {
  /**
   * Parallel execution of the <code>tasks</code>. The startup is synchronized so this method
   * emulates a load test.
   * @param tasks the list of tasks to be run
   * @param threadPoolSize the size of the parallel threads to be used to execute the tasks
   * @param timeout <b>CURRENTLY NOT USED</b> (a maximum timeout to wait for tasks finalization)
   * @param triggerAtOnce <tt>true</tt> if the parallel execution of tasks should be trigger at once
   */
  public static final void execute(List<? extends Runnable> tasks, int threadPoolSize,
      long timeout, boolean triggerAtOnce) {
    final CountDownLatch startGate= new CountDownLatch(1);
    final CountDownLatch endGate= new CountDownLatch(tasks.size());

    ExecutorService pooledExecutor= Executors.newFixedThreadPool(threadPoolSize);
    for(final Runnable tsk: tasks) {
      try {
        pooledExecutor.execute(new CountDownLatchedRunnable(tsk, endGate, triggerAtOnce ? startGate : null));
      }
      catch(RejectedExecutionException reex) {
        ; // this should never happen as we submit all tasks at once
      }
    }
    try {
      startGate.countDown();
      endGate.await();
      pooledExecutor.shutdown();
    }
    catch(InterruptedException e) {
      Thread.currentThread().interrupt();
      log(2, "Error waiting for concurrent executors to finish " + e.getMessage());
    }
  }

  public static void execute(MapList<Integer, TestMethodWorker> sequentialWorkers) {
    int nThreads = 3;
    GroupThreadPoolExecutor executor = new GroupThreadPoolExecutor(nThreads, nThreads,
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(),
        sequentialWorkers);
    
    executor.run();
    try {
      executor.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Returns a readable name of the current executing thread.
   */
  public static final String currentThreadInfo() {
    Thread thread= Thread.currentThread();
    return String.valueOf(thread.getName() + "@" + thread.hashCode());
  }

  public static final IExecutor createExecutor(int threadCount, String threadFactoryName) {
    return new ExecutorAdapter(threadCount, createFactory(threadFactoryName));
  }

  public static final IAtomicInteger createAtomicInteger(int initialValue) {
    return new AtomicIntegerAdapter(initialValue);
  }

  private static final IThreadFactory createFactory(String name) {
    return new ThreadFactoryImpl(name);
  }

  /*private static final ICountDown createCountDown(int count) {
    return new CountDownAdapter(count);
  }*/

  private static void log(int level, String msg) {
    Utils.log("ThreadUtil:" + ThreadUtil.currentThreadInfo(), level, msg);
  }

  /*private static final IPooledExecutor createPooledExecutor(int size) {
    return new PooledExecutorAdapter(size);
  }*/

  public static class ThreadFactoryImpl implements IThreadFactory, ThreadFactory {
    private String m_methodName;
    private List<Thread> m_threads = new ArrayList<Thread>();

    public ThreadFactoryImpl(String name) {
      m_methodName= name;
    }

    public Thread newThread(Runnable run) {
      Thread result = new TestNGThread(run, m_methodName);
      m_threads.add(result);
      return result;
    }

    public Object getThreadFactory() {
      return this;
    }

    public List<Thread> getThreads() {
      return m_threads;
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
