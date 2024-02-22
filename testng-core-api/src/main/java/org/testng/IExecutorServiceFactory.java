package org.testng;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

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
}
