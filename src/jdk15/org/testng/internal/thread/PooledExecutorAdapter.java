package org.testng.internal.thread;


import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.testng.TestNGException;

/**
 * An <code>IPooledExecutor</code> implementation based on JDK1.5 native support.
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>the_mindstorm</a>
 */
public class PooledExecutorAdapter extends ThreadPoolExecutor implements IPooledExecutor {
  public PooledExecutorAdapter(int noThreads) {
    super(noThreads,
          noThreads,
          0L,
          TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
  }

  public void execute(Runnable command) {
    try {
      super.execute(command);
    }
    catch(RejectedExecutionException ree) {
      throw new TestNGException("Task was not accepted for execution", ree);
    }
  }

  public void awaitTermination(long timeout) throws InterruptedException {
    super.awaitTermination(timeout, TimeUnit.MILLISECONDS);
  }
}
