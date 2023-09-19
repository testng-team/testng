package org.testng.internal;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import org.testng.TestNGException;
import org.testng.collections.Lists;
import org.testng.internal.thread.ThreadUtil;

/** Simple wrapper for an ExecutorCompletionService. */
public class PoolService<FutureType> implements Closeable {

  private final ExecutorCompletionService<FutureType> m_completionService;
  private final ExecutorService m_executor;

  private final boolean shutdownAfterExecution;

  public PoolService(int threadPoolSize) {
    this(threadPoolSize, true);
  }

  public PoolService(int threadPoolSize, boolean shutdownAfterExecution) {

    ThreadFactory threadFactory =
        new ThreadFactory() {

          private final AtomicInteger threadNumber = new AtomicInteger(0);

          @Override
          public Thread newThread(@Nonnull Runnable r) {
            return new Thread(
                r, ThreadUtil.THREAD_NAME + "-PoolService-" + threadNumber.getAndIncrement());
          }
        };
    m_executor = Executors.newFixedThreadPool(threadPoolSize, threadFactory);
    m_completionService = new ExecutorCompletionService<>(m_executor);
    this.shutdownAfterExecution = shutdownAfterExecution;
  }

  public List<FutureType> submitTasksAndWait(List<? extends Callable<FutureType>> tasks) {

    List<Future<FutureType>> takes = Lists.newArrayList(tasks.size());
    for (Callable<FutureType> callable : tasks) {
      takes.add(m_completionService.submit(callable));
    }

    List<FutureType> result = Lists.newArrayList(takes.size());
    for (Future<FutureType> take : takes) {
      try {
        result.add(take.get());
      } catch (InterruptedException | ExecutionException e) { // NOSONAR
        throw new TestNGException(e);
      }
    }

    if (shutdownAfterExecution) {
      m_executor.shutdown();
    }
    return result;
  }

  @Override
  public void close() throws IOException {
    if (!shutdownAfterExecution) {
      m_executor.shutdown();
    }
  }
}
