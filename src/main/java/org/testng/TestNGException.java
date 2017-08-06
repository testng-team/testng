package org.testng;

/**
 * The base class for all exceptions thrown by TestNG.
 */
public class TestNGException extends RuntimeException {

  public TestNGException(Throwable t) {
    super(t);
  }

  /**
   * @param string
   */
  public TestNGException(String string) {
    super("\n" + string);
  }

  public TestNGException(String string, Throwable t) {
    super("\n" + string, t);
  }
}
