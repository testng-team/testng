package test.skip.github1632;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(ListenerMarksMethodAsSkippedSample.MySkipTestListener.class)
public class ListenerMarksMethodAsSkippedSample {

  @BeforeMethod
  void beforeMethod() {}

  @Test
  void shouldNotBeExecuted() {}

  public static class MySkipTestListener implements IInvokedMethodListener {

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
      testResult.setStatus(ITestResult.SKIP);
    }
  }
}
