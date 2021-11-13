package test.skip.github1632;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

// Sample from
// https://github.com/spring-projects/spring-framework/issues/26387#issuecomment-760326643
@Listeners(BeforeListenerSkipSample.MySkipTestListener.class)
public class BeforeListenerSkipSample {

  @BeforeMethod
  void beforeMethod() {}

  @Test
  void shouldNotBeExecuted() {}

  public static class MySkipTestListener implements IInvokedMethodListener {

    @Override
    public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult testResult) {
      throw new SkipException("skip");
    }
  }
}
