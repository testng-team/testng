package org.testng;

/**
 * Exception thrown when an exception happens while running a test
 * method.
 *
 * @author Cedric Beust, Apr 26, 2004
 *
 */
public class TestException extends TestNGException {
	private static final long serialVersionUID = -7946644025188038804L;

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
