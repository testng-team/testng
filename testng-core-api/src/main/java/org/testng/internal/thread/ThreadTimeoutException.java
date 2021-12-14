package org.testng.internal.thread;

import org.testng.ITestNGMethod;

/** Exception used to signal a thread timeout. */
public class ThreadTimeoutException extends Exception {

  private static final long serialVersionUID = 7009400729783393548L;

  public ThreadTimeoutException(String msg) {
    super(msg);
  }

  public ThreadTimeoutException(Throwable cause) {
    super(cause);
  }

  public ThreadTimeoutException(ITestNGMethod tm, long timeout) {
    this("Method " + tm.getQualifiedName() + "() didn't finish within the time-out " + timeout);
  }
}
