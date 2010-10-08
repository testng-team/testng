package org.testng;

/**
 * If a test class implements this interface, its run() method
 * will be invoked instead of each configuration method found.  The invocation of
 * the configuration method will then be performed upon invocation of the callBack()
 * method of the IConfigureCallBack parameter.
 *
 * @author cbeust
 * Sep 07, 2010
 */
public interface IConfigurable extends ITestNGListener {
  public void run(IConfigureCallBack callBack, ITestResult testResult);
}
