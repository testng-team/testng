package test.configuration.issue3006;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestClassSample {

  public static ITestResult iTestResult;

  @BeforeMethod
  public void beforeMethod() {
    throw new RuntimeException("Exception to simulate configuration error");
  }

  @Test
  public void test() {}

  @AfterMethod(alwaysRun = true)
  public void afterMethod(ITestResult testResult) {
    iTestResult = testResult;
  }
}
