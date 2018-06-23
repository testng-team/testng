package org.testng.internal.thread;

import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A helper class to interface TestNG concurrency usage.
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>Alex Popescu</a>
 */
public class ThreadUtil {

  public static final String THREAD_NAME = "TestNG";

  /** @return true if the current thread was created by TestNG. */
  public static boolean isTestNGThread() {
    return Thread.currentThread().getName().contains(THREAD_NAME);
  }

  /**
   * Parallel execution of the <code>tasks</code>. The startup is synchronized so this method
   * emulates a load test.
   *
   * @param tasks the list of tasks to be run
   * @param threadPoolSize the size of the parallel threads to be used to execute the tasks
   * @param timeout a maximum timeout to wait for tasks finalization
   * @param triggerAtOnce <tt>true</tt> if the parallel execution of tasks should be trigger at once
   */
  public static void execute(
      String name,
      List<? extends Runnable> tasks,
      int threadPoolSize,
      long timeout,
      boolean triggerAtOnce) {

    Utils.log(
        "ThreadUtil",
        2,
        "Starting executor timeOut:"
            + timeout
            + "ms"
            + " workers:"
            + tasks.size()
            + " threadPoolSize:"
            + threadPoolSize);
    ExecutorService pooledExecutor =
        new ThreadPoolExecutor(
            threadPoolSize,
            threadPoolSize,
            timeout,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new TestNGThreadFactory(name));

    List<Callable<Object>> callables = Lists.newArrayList();
    for (final Runnable task : tasks) {
      callables.add(
          new Callable<Object>() {

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
      Logger.getLogger(ThreadUtil.class).error(handled.getMessage(), handled);
      Thread.currentThread().interrupt();
    } finally {
      pooledExecutor.shutdown();
    }
  }

  /** Returns a readable name of the current executing thread. */
  public static String currentThreadInfo() {
    Thread thread = Thread.currentThread();
    return String.valueOf(thread.getName() + "@" + thread.hashCode());
  }

  public static IExecutor createExecutor(int threadCount, String threadFactoryName) {
    return new ExecutorAdapter(threadCount, createFactory(threadFactoryName));
  }

  private static IThreadFactory createFactory(String name) {
    return new ThreadFactoryImpl(name);
  }

  public static class ThreadFactoryImpl extends TestNGThreadFactory implements IThreadFactory {

    private final List<Thread> threads = Lists.newArrayList();

    public ThreadFactoryImpl(String name) {
      super("method=" + name);
    }

    @Override
    public Object getThreadFactory() {
      return this;
    }

    @Override
    public Thread newThread(Runnable r) {
      Thread t = super.newThread(r);
      threads.add(t);
      return t;
    }

    @Override
    public List<Thread> getThreads() {
      return threads;
    }
  }
}
