package org.testng.configuration.samples.issue1753;

import java.lang.reflect.Method;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class ParentClassSample {
  // This one passes so that ChildClassSample#childClassBeforeMethod, which is the @BeforeMethod
  // that fails in this hierarchy, still runs and still contributes its attribute: since
  // GITHUB-1622 a @Before method is not run at all once a configuration before it has failed,
  // alwaysRun or not. FailingParentClassSample covers the other way round.
  @BeforeMethod(alwaysRun = true)
  public void parentClassBeforeMethod(Method method, ITestResult result) {
    result.setAttribute(
        getClass().getSimpleName() + "-parentClassBeforeMethod",
        getClass().getName() + ".parentClassBeforeMethod()");
  }

  @AfterMethod(alwaysRun = true)
  public void parentClassAfterMethod(Method method, ITestResult result) {
    result.setAttribute(
        getClass().getSimpleName() + "-parentClassAfterMethod",
        getClass().getName() + ".parentClassAfterMethod()");
    throw new RuntimeException("Forcing a failure");
  }
}
