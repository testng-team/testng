package org.testng.internal.thread;

/**
 * Wrapper exception for ExecutionExceptions.
 */
public class ThreadExecutionException extends Exception {

   public ThreadExecutionException(Throwable t) {
		super(t);
	}
}