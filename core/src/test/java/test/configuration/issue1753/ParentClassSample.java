package test.configuration.issue1753;

import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

public class ParentClassSample {
  @BeforeMethod(alwaysRun = true)
  public void parentClassBeforeMethod(Method method, ITestResult result) {
    result.setAttribute(
        getClass().getSimpleName() + "-parentClassBeforeMethod",
        getClass().getName() + ".parentClassBeforeMethod()");
    throw new RuntimeException("Forcing a failure");
  }

  @AfterMethod(alwaysRun = true)
  public void parentClassAfterMethod(Method method, ITestResult result) {
    result.setAttribute(
        getClass().getSimpleName() + "-parentClassAfterMethod",
        getClass().getName() + ".parentClassAfterMethod()");
    throw new RuntimeException("Forcing a failure");
  }
}
