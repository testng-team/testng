/*
 * $Id$
 * $Date$
 */
package org.testng.internal.thread;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class javadoc XXX
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>the_mindstorm</a>
 * @version $Revision$
 */
public class PooledExecutorAdapter extends ThreadPoolExecutor implements IPooledExecutor {
   public PooledExecutorAdapter(int noThreads) {
      super(noThreads,
            noThreads,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
   }

   public void awaitTermination(long timeout) throws InterruptedException {
      super.awaitTermination(timeout, TimeUnit.MILLISECONDS);
   }
}