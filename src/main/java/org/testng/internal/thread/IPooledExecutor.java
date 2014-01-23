package org.testng.internal.thread;



/**
 * Reduced interface to mimic <code>ExecutorService</code>.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public interface IPooledExecutor {
  void execute(Runnable run);

  void shutdown();

  void awaitTermination(long timeout) throws InterruptedException;

  /**
   * @return
   */
  boolean isTerminated();
}
