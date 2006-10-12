package org.testng.internal.thread;


import java.util.List;

import org.testng.internal.Utils;
import org.testng.internal.thread.IPooledExecutor;
import org.testng.internal.thread.ThreadUtil;

/**
 * Utility class to level up threading according to JDK 1.4
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class ThreadUtil {

  /**
   *
   * @param tasks List<Runnable>
   * @param threadPoolSize
   * @param defaultTimeout
   */
  public static final void execute(List tasks, int threadPoolSize, long defaultTimeout) {
    IPooledExecutor executor= ThreadUtil.createPooledExecutor(threadPoolSize);

    for(int i= 0; i < tasks.size(); i++) {
      Runnable task= (Runnable) tasks.get(i);
      executor.execute(task);
    }

    try {
      executor.shutdown();
      log(3, "Waiting for termination, timeout:" + defaultTimeout);
      executor.awaitTermination(defaultTimeout);
      if(executor.isTerminated()) {
        log(3, "Timeout period passed, successful termination");
      }
      else {
        log(3, "Timeout period passed, execution not finished, waiting complete termination");
        while(!executor.isTerminated()) {
          executor.awaitTermination(100L);
        }
        log(3, "Successful complete termination");
      }
    }
    catch(InterruptedException e) {
      Utils.log("ThreadUtil:" + ThreadUtil.currentThreadInfo(), 3, "Error shutting down PooledExecutor " + e.getMessage());
    }
  }

  private static void log(int level, String msg) {
    Utils.log("ThreadUtil:" + ThreadUtil.currentThreadInfo(), level, msg);
  }

  public static final String currentThreadInfo() {
    return String.valueOf(Thread.currentThread().hashCode());
  }

  public static final ICountDown createCountDown(int count) {
    return new org.testng.internal.thread.port.CountDownAdapter(count);
  }

  public static final IExecutor createExecutor(int threadCount, IThreadFactory itf) {
    return new org.testng.internal.thread.port.ExecutorAdapter(threadCount, itf);
  }

  public static final IPooledExecutor createPooledExecutor(int size) {
    return new org.testng.internal.thread.port.PooledExecutorAdapter(size);
  }

  public static final IThreadFactory createFactory(String name) {
    return new ThreadFactoryImpl(name);
  }

  public static class ThreadFactoryImpl implements IThreadFactory,
    edu.emory.mathcs.backport.java.util.concurrent.ThreadFactory {
    private String m_methodName;

    public ThreadFactoryImpl(String name) {
      m_methodName= name;
    }

    public Thread newThread(Runnable run) {
      return new TestNGThread(run, m_methodName);
    }

    public Object getThreadFactory() {
      return this;
    }
  }
}
