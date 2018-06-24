package test.configuration.issue1753;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class ChildClassSample extends ParentClassSample {
  @BeforeMethod(alwaysRun = true)
  public void childClassBeforeMethod(Method method, ITestResult result) {
    result.setAttribute(
        getClass().getSimpleName() + "-childClassBeforeMethod",
        getClass().getName() + ".childClassBeforeMethod()");
    throw new RuntimeException("Forcing a failure");
  }

  @Test
  public void testMethod() {}

  @AfterMethod(alwaysRun = true)
  public void childClassAfterMethod(Method method, ITestResult result) {
    result.setAttribute(
        getClass().getSimpleName() + "-childClassAfterMethod",
        getClass().getName() + ".childClassAfterMethod()");
  }
}
