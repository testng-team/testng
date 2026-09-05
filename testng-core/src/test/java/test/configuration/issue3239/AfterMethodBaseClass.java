package test.configuration.issue3239;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class AfterMethodBaseClass {

  @BeforeMethod(groups = "beforeMethod")
  public void beforeMethod() {}

  @AfterMethod(alwaysRun = true, dependsOnGroups = "beforeMethod")
  public void afterMethod() {}
}
