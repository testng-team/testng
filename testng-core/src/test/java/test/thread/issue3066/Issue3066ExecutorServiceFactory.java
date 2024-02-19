package test.thread.issue3066;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.testng.IExecutorServiceFactory;

public class Issue3066ExecutorServiceFactory implements IExecutorServiceFactory {
  @Override
  public ExecutorService create(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      TimeUnit unit,
      BlockingQueue<Runnable> workQueue,
      ThreadFactory threadFactory) {
    return new Issue3066ThreadPoolExecutor(
        corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
  }
}
