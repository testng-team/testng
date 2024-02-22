package test.thread.issue3066;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class Issue3066ThreadPoolExecutor extends ThreadPoolExecutor {

  private static boolean invoked = false;

  public Issue3066ThreadPoolExecutor(
      int corePoolSize,
      int maximumPoolSize,
      long keepAliveTime,
      @NotNull TimeUnit unit,
      @NotNull BlockingQueue<Runnable> workQueue,
      @NotNull ThreadFactory threadFactory) {
    super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    setInvoked();
  }

  private static void setInvoked() {
    Issue3066ThreadPoolExecutor.invoked = true;
  }

  public static boolean isInvoked() {
    return invoked;
  }

  public static void resetInvokedState() {
    invoked = false;
  }
}
