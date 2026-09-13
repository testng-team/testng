package test.configuration.issue1753;

import java.lang.reflect.Method;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChildOfFailingParentSample extends FailingParentClassSample {

  // Never runs: the parent @BeforeMethod above it failed (GITHUB-1622).
  @BeforeMethod(alwaysRun = true)
  public void childClassBeforeMethod(Method method, ITestResult result) {
    result.setAttribute(
        getClass().getSimpleName() + "-childClassBeforeMethod",
        getClass().getName() + ".childClassBeforeMethod()");
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
