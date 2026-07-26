package org.testng;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents the capability to create a custom {@link ExecutorService} by downstream consumers. The
 * implementation can be plugged in via the configuration parameter <code>-threadpoolfactoryclass
 * </code>
 */
@FunctionalInterface
public interface IExecutorServiceFactory {

  /**
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     {@code allowCoreThreadTimeOut} is set
   * @param maximumPoolSize the maximum number of threads to allow in the pool
   * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
   *     time that excess idle threads will wait for new tasks before terminating.
   * @param unit the time unit for the {@code keepAliveTime} argument
   * @param workQueue the queue to use for holding tasks before they are executed. This queue will
   *     hold only the {@code Runnable} tasks submitted by the {@code execute} method.
   * @param threadFactory the factory to use when the executor creates a new thread *
   * @return - An implementation of {@link ExecutorService}
   */
  ExecutorService create(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory);

  /**
   * Creates the single, common {@link ExecutorService} that is shared between regular test methods
   * and their (parallel) data-driven invocations when <code>use-global-thread-pool</code> is
   * enabled. Override this to plug in a custom implementation for that shared pool.
   *
   * <p><strong>Important:</strong> the returned service should be a {@link ForkJoinPool} (as the
   * default implementation is). With the shared pool, a data-driven test method waits for its
   * data-row tasks by submitting them back into this <em>same</em> pool; only a {@link
   * ForkJoinPool} lets that waiting worker help run those tasks (work-stealing) instead of parking
   * a thread. Returning a pool that cannot work-steal (for example a plain {@link
   * java.util.concurrent.ThreadPoolExecutor}) re-introduces the throttled parallelism and
   * dead-locks that GITHUB-3242 addressed, so only do so if you size the pool accordingly.
   *
   * @param parallelism the desired parallelism, i.e. the configured <code>thread-count</code>
   * @param threadNamePrefix the prefix to use when naming the pool's worker threads (each thread is
   *     named <code>threadNamePrefix-N</code>)
   * @return the {@link ExecutorService} to be shared for the whole suite
   */
  default ExecutorService createGlobalThreadPool(int parallelism, String threadNamePrefix) {
    AtomicInteger threadNumber = new AtomicInteger(1);
    ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory =
        pool -> {
          ForkJoinWorkerThread thread =
              ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
          thread.setName(threadNamePrefix + "-" + threadNumber.getAndIncrement());
          return thread;
        };
    return new ForkJoinPool(Math.max(parallelism, 1), threadFactory, null, /* asyncMode= */ true);
  }
}
