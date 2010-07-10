package org.testng;

/**
 * Implement this interface if you need a handle to {@link ITestContext}.
 *
 * @author karthik.krishnan@gmail.com (Karthik Krishnan)
 */
public interface IInvokedMethodListener2 extends IInvokedMethodListener {

  /**
   * To be implemented if the method needs a handle to contextual information.
   */
  void beforeInvocation(IInvokedMethod method, ITestResult testResult,
      ITestContext context);

  /**
   * To be implemented if the method needs a handle to contextual information.
   */
  void afterInvocation(IInvokedMethod method, ITestResult testResult,
      ITestContext context);

}
