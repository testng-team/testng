package org.testng.internal.thread;

/** Wrapper exception for ExecutionExceptions. */
public class ThreadExecutionException extends Exception {

  private static final long serialVersionUID = -7766644143333236263L;

  public ThreadExecutionException(Throwable t) {
    super(t);
  }
}
