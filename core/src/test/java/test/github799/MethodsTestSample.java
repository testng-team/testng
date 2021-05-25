package test.github799;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class MethodsTestSample {
  @Test
  public void angry() {
    String methodName = Reporter.getCurrentTestResult().getMethod().getMethodName();
    Reporter.log(methodName);
    Assert.assertNotNull(methodName);
  }

  @Test
  public void birds() {
    String methodName = Reporter.getCurrentTestResult().getMethod().getMethodName();
    Reporter.log(methodName);
    Assert.assertNotNull(methodName);
  }

  @Test
  public void android() {
    String methodName = Reporter.getCurrentTestResult().getMethod().getMethodName();
    Reporter.log(methodName);
    Assert.assertNotNull(methodName);
  }
}
