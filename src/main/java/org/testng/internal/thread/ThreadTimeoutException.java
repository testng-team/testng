package org.testng.internal.thread;

/**
 * Exception used to signal a thread timeout.
 *
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class ThreadTimeoutException extends Exception {
  static final long serialVersionUID = 7009400729783393548L;

   public ThreadTimeoutException(String msg) {
      super(msg);
   }

   public ThreadTimeoutException(Throwable cause) {
      super(cause);
   }

   public ThreadTimeoutException(String msg, Throwable cause) {
      super(msg, cause);
   }
}