package org.testng.internal.thread.port;

import org.testng.internal.thread.ICountDown;
import edu.emory.mathcs.util.concurrent.CountDownLatch;
import edu.emory.mathcs.util.concurrent.TimeUnit;

/**
 * CountDownLatch adapter.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class CountDownAdapter implements ICountDown {
   protected CountDownLatch m_doneLatch;

   public CountDownAdapter(int count) {
      m_doneLatch = new CountDownLatch(count);
   }

   public void await() throws InterruptedException {
      m_doneLatch.await();
   }

   public boolean await(long timeout) throws InterruptedException {
      return m_doneLatch.await(timeout, TimeUnit.MILLISECONDS);
   }

   public void countDown() {
      m_doneLatch.countDown();
   }
}