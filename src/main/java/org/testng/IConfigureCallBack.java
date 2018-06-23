package org.testng;

/**
 * A parameter of this type will be passed to the run() method of a IConfigurable. Invoking
 * runConfigurationMethod() on that parameter will cause the test method currently being diverted to
 * be invoked.
 *
 * <p><b>This interface is not meant to be implemented by clients, only by TestNG.</b>
 *
 * @see org.testng.IConfigurable
 * @author cbeust Sep 07, 2010
 */
public interface IConfigureCallBack {

  /** Invoke the test method currently being hijacked. */
  void runConfigurationMethod(ITestResult testResult);

  /** @return the parameters that will be used to invoke the configuration method. */
  Object[] getParameters();
}
