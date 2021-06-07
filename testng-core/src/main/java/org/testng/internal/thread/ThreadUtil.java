package org.testng.internal.thread;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;

/** A helper class to interface TestNG concurrency usage. */
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
   */
  public static void execute(
      String name, List<? extends Runnable> tasks, int threadPoolSize, long timeout) {

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
            new LinkedBlockingQueue<>(),
            new TestNGThreadFactory(name));

    List<Callable<Object>> callables = Lists.newArrayList();
    for (final Runnable task : tasks) {
      callables.add(
          () -> {
            task.run();
            return null;
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
    return thread.getName() + "@" + thread.hashCode();
  }

  public static ExecutorService createExecutor(int threadCount, String threadFactoryName) {
    ThreadFactory tf = new TestNGThreadFactory("method=" + threadFactoryName);
    return new ThreadPoolExecutor(
        threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), tf);
  }
}
