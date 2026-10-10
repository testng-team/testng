package org.testng;

/**
 * The failure that TestNG reports when a test method does not throw what {@code expectedExceptions}
 * asks for.
 *
 * <p>This happens when the method throws no exception, or an exception of another type. It also
 * happens when the message of the exception does not match {@code expectedExceptionsMessageRegExp}.
 */
public class TestException extends TestNGException {

  private static final long serialVersionUID = -7946644025188038804L;

  /**
   * Creates the exception with a message.
   *
   * @param s the message.
   */
  public TestException(String s) {
    super(s);
  }

  /**
   * Creates the exception with a cause.
   *
   * @param t the exception that the test method threw.
   */
  public TestException(Throwable t) {
    super(t);
  }

  /**
   * Creates the exception with a message and a cause.
   *
   * @param message the message.
   * @param t the exception that the test method threw.
   */
  public TestException(String message, Throwable t) {
    super(message, t);
  }
}
