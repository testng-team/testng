package org.testng;

/** The base class for all exceptions thrown by TestNG. */
public class TestNGException extends RuntimeException {

  private static final long serialVersionUID = -422675971506425913L;

  public TestNGException(Throwable t) {
    super(t);
  }

  /** @param string */
  public TestNGException(String string) {
    super("\n" + string);
  }

  public TestNGException(String string, Throwable t) {
    super("\n" + string, t);
  }
}
