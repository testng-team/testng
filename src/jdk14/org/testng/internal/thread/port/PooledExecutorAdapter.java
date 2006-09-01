package org.testng.internal.thread.port;

import org.testng.TestNGException;
import org.testng.internal.thread.IPooledExecutor;

import edu.emory.mathcs.util.concurrent.LinkedBlockingQueue;
import edu.emory.mathcs.util.concurrent.RejectedExecutionException;
import edu.emory.mathcs.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.util.concurrent.TimeUnit;

/**
 * IPooledExecutor implementation based on ThreadPoolExecutor.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class PooledExecutorAdapter extends ThreadPoolExecutor implements IPooledExecutor {
   public PooledExecutorAdapter(int poolSize) {
      super(poolSize,
            poolSize,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue());
   }

   
   public void execute(Runnable run) {
     try {
       super.execute(run);
     }
     catch(RejectedExecutionException ree) {
       throw new TestNGException("Task was not accepted for execution", ree);
     }
   }


  public void awaitTermination(long timeout) throws InterruptedException {
      super.awaitTermination(timeout, TimeUnit.MILLISECONDS);
   }
}