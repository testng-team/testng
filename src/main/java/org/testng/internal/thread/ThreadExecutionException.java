package org.testng.internal.thread;

/**
 * Wrapper exception for ExecutionExceptions.
 *
 * @author <a href="mailto:the_mindstorm@evolva.ro>the_mindstorm</a>
 */
public class ThreadExecutionException extends Exception {
	static final long serialVersionUID = -7766644143333236263L;

   public ThreadExecutionException(Throwable t) {
		super(t);
	}
}