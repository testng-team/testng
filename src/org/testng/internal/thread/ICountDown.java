package org.testng.internal.thread;

/**
 * Reduced interface to mimic a CountDownLatch.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public interface ICountDown {
   void await() throws InterruptedException;

   boolean await(long timeout) throws InterruptedException;

   void countDown();
}
