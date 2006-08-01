package org.testng.internal.thread;

/**
 * Utility class to level up threading according to JDK 1.4
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class ThreadUtil {
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

   public static class ThreadFactoryImpl implements IThreadFactory, edu.emory.mathcs.util.concurrent.ThreadFactory {
      private String m_methodName;

      public ThreadFactoryImpl(String name) {
         m_methodName = name;
      }

      public Thread newThread(Runnable run) {
         return new TestNGThread(run, m_methodName);
      }

      public Object getThreadFactory() {
         return this;
      }
   }
}