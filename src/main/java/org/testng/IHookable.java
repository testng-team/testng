package org.testng;

/**
 * If a test class implements this interface, its run() method
 * will be invoked instead of each @Test method found.  The invocation of
 * the test method will then be performed upon invocation of the callBack()
 * method of the IHookCallBack parameter.
 *
 * This is useful to test classes that require JAAS authentication, which can
 * be implemented as follows:
 *
 * <pre>
 * public void run(final IHookCallBack icb, ITestResult testResult) {
 *   // Preferably initialized in a @Configuration method
 *   mySubject = authenticateWithJAAs();
 *
 *   Subject.doAs(mySubject, new PrivilegedExceptionAction() {
 *     public Object run() {
 *       icb.callback(testResult);
 *     }
 *   };
 * }
 * </pre>
 *
 * @author cbeust
 * Jan 28, 2006
 */
public interface IHookable extends ITestNGListener {
  public void run(IHookCallBack callBack, ITestResult testResult);
}
