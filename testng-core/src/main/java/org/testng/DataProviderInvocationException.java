package org.testng;

/** Represents any issues that arise out of invoking a data provider method. */
public class DataProviderInvocationException extends TestNGException {

  public DataProviderInvocationException(String string, Throwable t) {
    super(string, t);
  }
}
