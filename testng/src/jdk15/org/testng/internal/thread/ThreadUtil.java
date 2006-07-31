/*
 * $Id$
 * $Date$
 */
package org.testng.internal.thread;

import org.testng.internal.thread.ExecutorAdapter;
import org.testng.internal.thread.PooledExecutorAdapter;

import java.util.concurrent.ThreadFactory;


/**
 * Class javadoc XXX
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>the_mindstorm</a>
 * @version $Revision$
 */
public class ThreadUtil {
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