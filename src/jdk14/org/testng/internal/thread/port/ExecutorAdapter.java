package org.testng.internal.thread.port;


import org.testng.internal.thread.IExecutor;
import org.testng.internal.thread.IFutureResult;
import org.testng.internal.thread.IThreadFactory;

import edu.emory.mathcs.backport.java.util.concurrent.ArrayBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

/**
 * ThreadPoolExecutor adaptor.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class ExecutorAdapter extends ThreadPoolExecutor implements IExecutor {
   public ExecutorAdapter(int threadCount, IThreadFactory tf) {
      super(threadCount,
            threadCount,
            0L,
            TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue(threadCount),
            (edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory) tf.getThreadFactory());
   }


   public IFutureResult submitRunnable(final Runnable runnable) {
      return new FutureResultAdapter(super.submit(runnable));
   }

   public boolean awaitTermination(long timeout) {
     boolean result= false;
     try {
      result= super.awaitTermination(timeout, TimeUnit.MILLISECONDS);
     }
     catch(InterruptedException iex) {
       System.out.println("[WARN] ThreadPoolExecutor has been interrupted while awaiting termination");
       Thread.currentThread().interrupt();
     }
     
     return result;
   }

   public void stopNow() {
      super.shutdownNow();
   }

}