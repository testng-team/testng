package org.testng.internal.thread;

/**
 * Reduced interface to mimic an ExecutorService.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public interface IExecutor {
   IFutureResult submitRunnable(Runnable runnable);

   void shutdown();

   boolean awaitTermination(long timeout);

   void stopNow();

   StackTraceElement[][] getStackTraces();
}
