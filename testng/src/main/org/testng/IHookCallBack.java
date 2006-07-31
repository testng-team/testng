package org.testng;

/**
 * A parameter of this type will be passed to the run() method of a IHookable.
 * Invoking runTestMethod() on that parameter will cause the test method currently
 * being diverted to be invoked.
 * 
 *  <p>
 *  
 * <b>This interface is not meant to be implemented by clients, only by TestNG.</b>
 * 
 * @see org.testng.IHookable
 * 
 * 
 * @author cbeust
 * @date Jan 28, 2006
 */
public interface IHookCallBack {
  
  /**
   * Invoke the test method currently being hijacked.
   */
  public void runTestMethod(ITestResult testResult);
}
