package org.testng.internal.thread;


import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * An implementation for <code>IExecutor</code> based on <code>ThreadPoolExecutor</code>
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>Alexandru Popescu</a>
 */
public class ExecutorAdapter extends ThreadPoolExecutor implements IExecutor {
  private IThreadFactory m_threadFactory;

  public ExecutorAdapter(int threadCount, IThreadFactory tf) {
      super(threadCount,
            threadCount,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            (ThreadFactory) tf.getThreadFactory());
      m_threadFactory = tf;
   }

   @Override
  public IFutureResult submitRunnable(final Runnable runnable) {
      return new FutureResultAdapter(super.submit(runnable));
   }

   @Override
  public void stopNow() {
      super.shutdownNow();
   }

   @Override
  public boolean awaitTermination(long timeout) {
     boolean result= false;
     try {
      result= super.awaitTermination(timeout, TimeUnit.MILLISECONDS);
     }
     catch(InterruptedException handled) {
       System.out.println("[WARN] ThreadPoolExecutor has been interrupted while awaiting termination");
       Thread.currentThread().interrupt();
     }

     return result;
   }

  @Override
  public StackTraceElement[][] getStackTraces() {
    List<Thread> threads = m_threadFactory.getThreads();
    int threadCount = threads.size();
    StackTraceElement[][] result = new StackTraceElement[threadCount][];
    for (int i = 0; i < result.length; i++) {
      result[i] = threads.get(i).getStackTrace();
    }
    return result;
  }
}