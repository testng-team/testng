package org.testng;

import org.checkerframework.checker.nullness.qual.Nullable;

/** The base class for all exceptions thrown by TestNG. */
public class TestNGException extends RuntimeException {

  private static final long serialVersionUID = -422675971506425913L;

  public TestNGException(Throwable t) {
    super(t);
  }

  public TestNGException(String string) {
    super("\n" + string);
  }

  public TestNGException(String string, @Nullable Throwable t) {
    super("\n" + string, t);
  }
}
