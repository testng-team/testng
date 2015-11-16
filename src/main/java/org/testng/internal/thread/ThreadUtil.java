package org.testng.internal.thread;

import org.testng.collections.Lists;
import org.testng.internal.Utils;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A helper class to interface TestNG concurrency usage.
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>Alex Popescu</a>
 */
public class ThreadUtil {
  private static final String THREAD_NAME = "TestNG";

  /**
   * @return true if the current thread was created by TestNG.
   */
  public static boolean isTestNGThread() {
    return Thread.currentThread().getName().contains(THREAD_NAME);
  }

  /**
   * Parallel execution of the <code>tasks</code>. The startup is synchronized so this method
   * emulates a load test.
   * @param tasks the list of tasks to be run
   * @param threadPoolSize the size of the parallel threads to be used to execute the tasks
   * @param timeout a maximum timeout to wait for tasks finalization
   * @param triggerAtOnce <tt>true</tt> if the parallel execution of tasks should be trigger at once
   */
  public static final void execute(List<? extends Runnable> tasks, int threadPoolSize,
      long timeout, boolean triggerAtOnce) {
    final CountDownLatch startGate= new CountDownLatch(1);
    final CountDownLatch endGate= new CountDownLatch(tasks.size());

    Utils.log("ThreadUtil", 2, "Starting executor timeOut:" + timeout + "ms"
        + " workers:" + tasks.size() + " threadPoolSize:" + threadPoolSize);
    ExecutorService pooledExecutor = // Executors.newFixedThreadPool(threadPoolSize);
        new ThreadPoolExecutor(threadPoolSize, threadPoolSize,
        timeout, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(),
        new ThreadFactory() {
          @Override
          public Thread newThread(Runnable r) {
            Thread result = new Thread(r);
            result.setName(THREAD_NAME);
            return result;
          }
        });

    List<Callable<Object>> callables = Lists.newArrayList();
    for (final Runnable task : tasks) {
      callables.add(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          task.run();
          return null;
        }

      });
    }
    try {
      if (timeout != 0) {
        pooledExecutor.invokeAll(callables, timeout, TimeUnit.MILLISECONDS);
      } else {
        pooledExecutor.invokeAll(callables);
      }
    } catch (InterruptedException handled) {
      handled.printStackTrace();
      Thread.currentThread().interrupt();
    } finally {
      pooledExecutor.shutdown();
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

  private static final IThreadFactory createFactory(String name) {
    return new ThreadFactoryImpl(name);
  }

  private static void log(int level, String msg) {
    Utils.log("ThreadUtil:" + ThreadUtil.currentThreadInfo(), level, msg);
  }

  public static class ThreadFactoryImpl implements IThreadFactory, ThreadFactory {
    private String m_methodName;
    private List<Thread> m_threads = Lists.newArrayList();

    public ThreadFactoryImpl(String name) {
      m_methodName= name;
    }

    @Override
    public Thread newThread(Runnable run) {
      Thread result = new TestNGThread(run, m_methodName);
      m_threads.add(result);
      return result;
    }

    @Override
    public Object getThreadFactory() {
      return this;
    }

    @Override
    public List<Thread> getThreads() {
      return m_threads;
    }
  }
}
