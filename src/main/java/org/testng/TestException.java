package org.testng;

/**
 * Exception thrown when an exception happens while running a test
 * method.
 */
public class TestException extends TestNGException {

  public TestException(String s) {
    super(s);
  }

	public TestException(Throwable t) {
		super(t);
	}

  public TestException(String message, Throwable t) {
    super(message, t);
  }
}
