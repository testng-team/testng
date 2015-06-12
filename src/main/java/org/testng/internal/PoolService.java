package org.testng.internal;

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

/**
 * Simple wrapper for an ExecutorCompletionService.
 */
public class PoolService<FutureType> {

  private ExecutorCompletionService<FutureType> m_completionService;
  private ThreadFactory m_threadFactory;
  private ExecutorService m_executor;

  public PoolService(int threadPoolSize) {
    m_threadFactory = new ThreadFactory() {
      private int m_threadIndex = 0;

      @Override
      public Thread newThread(Runnable r) {
        Thread result = new Thread(r);
        result.setName("PoolService-" + m_threadIndex);
        m_threadIndex++;
        return result;
      }
    };
    m_executor = Executors.newFixedThreadPool(threadPoolSize, m_threadFactory);
    m_completionService = new ExecutorCompletionService<>(m_executor);
  }

  public List<FutureType> submitTasksAndWait(List<? extends Callable<FutureType>> tasks) {
    List<FutureType> result = Lists.newArrayList();

    for (Callable<FutureType> callable : tasks) {
      m_completionService.submit(callable);
    }
    for (int i = 0; i < tasks.size(); i++) {
      try {
        Future<FutureType> take = m_completionService.take();
        result.add(take.get());
      } catch (InterruptedException | ExecutionException e) {
        throw new TestNGException(e);
      }
    }

    m_executor.shutdown();
    return result;
  }
}
