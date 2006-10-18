package org.testng.internal.thread;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import org.testng.ITestResult;
import org.testng.internal.TestMethodWorker;
import org.testng.internal.Utils;
import org.testng.internal.thread.ExecutorAdapter;
import org.testng.internal.thread.PooledExecutorAdapter;

/**
 * A helper class to interface TestNG concurrency usage.
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>Alex Popescu</a>
 */
public class ThreadUtil {
  public static final void execute(List<? extends Runnable> tasks, int threadPoolSize, long defaultTimeout) {
    IPooledExecutor executor= ThreadUtil.createPooledExecutor(threadPoolSize);

    for(Runnable task : tasks) {
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
          executor.awaitTermination(500L);
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
    return String.valueOf(Thread.currentThread().getId());
  }

  public static final ICountDown createCountDown(int count) {
    return new CountDownAdapter(count);
  }

  public static final IExecutor createExecutor(int threadCount, IThreadFactory itf) {
    return new ExecutorAdapter(threadCount, itf);
  }

  public static final IPooledExecutor createPooledExecutor(int size) {
    return new PooledExecutorAdapter(size);
  }

  public static final IThreadFactory createFactory(String name) {
    return new ThreadFactoryImpl(name);
  }

  public static class ThreadFactoryImpl implements IThreadFactory, ThreadFactory {
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
