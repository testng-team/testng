/*
 * $Id$
 * $Date$
 */
package org.testng.internal.thread;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class javadoc XXX
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>the_mindstorm</a>
 * @version $Revision$
 */
public class ExecutorAdapter extends ThreadPoolExecutor implements IExecutor {
   public ExecutorAdapter(int threadCount, IThreadFactory tf) {
      super(threadCount,
            threadCount,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            (ThreadFactory) tf.getThreadFactory());
   }

   public IFutureResult submitRunnable(final Runnable runnable) throws InterruptedException {
      return new FutureResultAdapter(super.submit(runnable));
   }

   public void stopNow() {
      super.shutdownNow();
   }

   public boolean awaitTermination(long timeout) throws InterruptedException {
      return super.awaitTermination(timeout, TimeUnit.MILLISECONDS);
   }
}