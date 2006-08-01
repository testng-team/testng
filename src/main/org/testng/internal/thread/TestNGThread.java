package org.testng.internal.thread;

/**
 * Custom named thread.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class TestNGThread extends Thread {
   public TestNGThread(String methodName) {
      super("TestNGInvoker-" + methodName + "()");
   }

   public TestNGThread(Runnable target, String methodName) {
      super(target, "TestNGInvoker-" + methodName + "()");
   }
}