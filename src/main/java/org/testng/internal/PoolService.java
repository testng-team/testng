package org.testng.internal;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.TestNGException;
import org.testng.collections.Lists;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import org.testng.internal.thread.ThreadUtil;

import javax.annotation.Nonnull;

/** Simple wrapper for an ExecutorCompletionService. */
public class PoolService<FutureType> {

  private final ExecutorCompletionService<FutureType> m_completionService;
  private final ExecutorService m_executor;

  public PoolService(int threadPoolSize) {

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

    m_executor.shutdown();
    return result;
  }
}
