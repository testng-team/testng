package test.configuration.issue3358;

import org.testng.annotations.BeforeMethod;

public class ParentFirstTimeOnlySample {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeParent() {}
}
