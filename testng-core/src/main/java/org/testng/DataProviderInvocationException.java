package org.testng;

/**
 * Represents any issues that arise out of invoking a data provider method.
 *
 * @deprecated - This class stands deprecated as of <code>7.8.0</code>
 */
@Deprecated
public class DataProviderInvocationException extends TestNGException {

  public DataProviderInvocationException(String string, Throwable t) {
    super(string, t);
  }
}
