package test_result;

import org.testng.Assert;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class AfterListenerSample {

  @Test
  public void failedTest() {}

  @Test(expectedExceptions = IllegalStateException.class, enabled = false)
  public void failedTest2() {
    throw new IllegalStateException();
  }

  @Test
  public void skippedTest() {}

  @Test
  public void succeedTest() {
    Assert.fail();
  }

  @Test
  public void succeedTest2() {
    throw new IllegalStateException();
  }

  public static class MySkipTestListener implements IInvokedMethodListener {

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
      switch (method.getTestMethod().getMethodName()) {
        case "failedTest":
        case "failedTest2":
          result.setStatus(ITestResult.FAILURE);
          break;
        case "skippedTest":
          result.setStatus(ITestResult.SKIP);
          break;
        case "succeedTest":
        case "succeedTest2":
          result.setStatus(ITestResult.SUCCESS);
          break;
        default:
          throw new RuntimeException(
              "Unknown method name: " + method.getTestMethod().getMethodName());
      }
    }
  }
}
