package test.expectedexceptions.issue2788;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import test.expectedexceptions.issue2788.TestClassSample.Local;

@Listeners(Local.class)
public class TestClassSample {

  @Test(expectedExceptions = NullPointerException.class)
  public void sampleTestMethod() {}

  public static class Local implements IInvokedMethodListener {

    public static Local instance;
    private boolean pass;

    private static void setInstance(Local localInstance) {
      instance = localInstance;
    }

    public static Local getInstance() {
      return instance;
    }

    public Local() {
      setInstance(this);
    }

    public boolean isPass() {
      return pass;
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
      pass = testResult.isSuccess();
    }
  }
}
