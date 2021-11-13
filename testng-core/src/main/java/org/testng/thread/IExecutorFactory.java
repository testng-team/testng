package org.testng.thread;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.testng.IDynamicGraph;
import org.testng.ISuite;
import org.testng.ITestNGMethod;

/**
 * Represents the capabilities to be possessed by any implementation that can be plugged into TestNG
 * to execute nodes from a {@link org.testng.IDynamicGraph} object.
 */
public interface IExecutorFactory {

  /**
   * @param name - The name to be used as a prefix for all created threads.
   * @param graph - A {@link org.testng.IDynamicGraph} object that represents the graph of methods
   *     and the hierarchy of execution.
   * @param factory - A {@link IThreadWorkerFactory} factory to create threads.
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     {@code allowCoreThreadTimeOut} is set
   * @param maximumPoolSize the maximum number of threads to allow in the pool
   * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
   *     time that excess idle threads will wait for new tasks before terminating.
   * @param unit the time unit for the {@code keepAliveTime} argument
   * @param workQueue the queue to use for holding tasks before they are executed. This queue will
   *     hold only the {@code Runnable} tasks submitted by the {@code execute} method.
   * @param comparator - A {@link Comparator} to order nodes internally.
   * @return - A new {@link ITestNGThreadPoolExecutor} that is capable of running suites in
   *     parallel.
   */
  ITestNGThreadPoolExecutor newSuiteExecutor(
      String name,
      IDynamicGraph<ISuite> graph,
      IThreadWorkerFactory<ISuite> factory,
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      Comparator<ISuite> comparator);

  /**
   * @param name - The name to be used as a prefix for all created threads.
   * @param graph - A {@link IDynamicGraph} object that represents the graph of methods and the
   *     hierarchy of execution.
   * @param factory - A {@link IThreadWorkerFactory} factory to create threads.
   * @param corePoolSize the number of threads to keep in the pool, even if they are idle, unless
   *     {@code allowCoreThreadTimeOut} is set
   * @param maximumPoolSize the maximum number of threads to allow in the pool
   * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
   *     time that excess idle threads will wait for new tasks before terminating.
   * @param unit the time unit for the {@code keepAliveTime} argument
   * @param workQueue the queue to use for holding tasks before they are executed. This queue will
   *     hold only the {@code Runnable} tasks submitted by the {@code execute} method.
   * @param comparator - A {@link Comparator} to order nodes internally.
   * @return - A new {@link ITestNGThreadPoolExecutor} that is capable of running test methods in
   *     parallel.
   */
  ITestNGThreadPoolExecutor newTestMethodExecutor(
      String name,
      IDynamicGraph<ITestNGMethod> graph,
      IThreadWorkerFactory<ITestNGMethod> factory,
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      Comparator<ITestNGMethod> comparator);
}
