package org.testng.internal.thread;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.testng.IDynamicGraph;
import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.internal.thread.graph.GraphThreadPoolExecutor;
import org.testng.thread.IExecutorFactory;
import org.testng.thread.ITestNGThreadPoolExecutor;
import org.testng.thread.IThreadWorkerFactory;

/**
 * @deprecated - This implementation stands deprecated as of TestNG <code>v7.9.0</code>. There are
 *     no alternatives for this implementation.
 */
@Deprecated
public class DefaultThreadPoolExecutorFactory implements IExecutorFactory {

  @Override
  public ITestNGThreadPoolExecutor newSuiteExecutor(
      String name,
      IDynamicGraph<ISuite> graph,
      IThreadWorkerFactory<ISuite> factory,
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      Comparator<ISuite> comparator) {
    return new GraphThreadPoolExecutor<>(
        name,
        graph,
        factory,
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        comparator);
  }

  @Override
  public ITestNGThreadPoolExecutor newTestMethodExecutor(
      String name,
      IDynamicGraph<ITestNGMethod> graph,
      IThreadWorkerFactory<ITestNGMethod> factory,
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      Comparator<ITestNGMethod> comparator) {
    return new GraphThreadPoolExecutor<>(
        name,
        graph,
        factory,
        corePoolSize,
        maximumPoolSize,
        keepAliveTime,
        unit,
        workQueue,
        comparator);
  }
}
