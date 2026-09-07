package test.skip.github1632;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(NoConfigAfterListenerSample.MySkipTestListener.class)
public class NoConfigAfterListenerSample {

  @Test
  void shouldNotBeExecuted() {}

  public static class MySkipTestListener implements IInvokedMethodListener {

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
      throw new SkipException("skip");
    }
  }
}
