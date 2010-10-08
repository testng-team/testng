/*
 * $Id$
 * $Date$
 */
package org.testng.internal.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * ICountDown adapter using the features in JDK 1.5, e.g.
 * <CODE>CountDownLatch</CODE>.
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>the_mindstorm</a>
 * @version $Revision$
 */
public class CountDownAdapter implements ICountDown {
   protected CountDownLatch m_doneLatch;

   public CountDownAdapter(int count) {
      m_doneLatch = new CountDownLatch(count);
   }

   @Override
  public void await() throws InterruptedException {
      m_doneLatch.await();
   }

   @Override
  public boolean await(long timeout) throws InterruptedException {
      return m_doneLatch.await(timeout, TimeUnit.MILLISECONDS);
   }

   @Override
  public void countDown() {
      m_doneLatch.countDown();
   }
}