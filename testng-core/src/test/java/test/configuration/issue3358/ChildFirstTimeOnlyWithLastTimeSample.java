package test.configuration.issue3358;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChildFirstTimeOnlyWithLastTimeSample extends ParentFirstTimeOnlySample {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeChild() {}

  @AfterMethod(lastTimeOnly = true)
  public void afterLast() {}

  @Test(invocationCount = 3)
  public void test() {}
}
