package org.testng.internal.thread;

/**
 * Reduced interface to mimic Future.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public interface IFutureResult {
   Object get() throws InterruptedException, ThreadExecutionException;
}
