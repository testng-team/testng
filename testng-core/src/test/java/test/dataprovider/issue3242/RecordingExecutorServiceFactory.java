package test.dataprovider.issue3242;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.testng.IExecutorServiceFactory;

/**
 * A custom {@link IExecutorServiceFactory} that records whether it was asked to build the shared
 * global thread-pool. It delegates to the default (work-stealing {@link
 * java.util.concurrent.ForkJoinPool}) implementation so the GITHUB-3242 fix still applies.
 *
 * <p>The recording is instance state, so a fresh factory is created per test and holds no shared
 * static state - it stays correct even if these unit tests are themselves run in parallel.
 */
public class RecordingExecutorServiceFactory implements IExecutorServiceFactory {

  private final AtomicBoolean globalPoolCreated = new AtomicBoolean(false);

  public boolean wasGlobalPoolCreated() {
    return globalPoolCreated.get();
  }

  @Override
  public ExecutorService create(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory) {
    return new ThreadPoolExecutor(
        corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
  }

  @Override
  public ExecutorService createGlobalThreadPool(int parallelism, String threadNamePrefix) {
    globalPoolCreated.set(true);
    return IExecutorServiceFactory.super.createGlobalThreadPool(parallelism, threadNamePrefix);
  }
}
