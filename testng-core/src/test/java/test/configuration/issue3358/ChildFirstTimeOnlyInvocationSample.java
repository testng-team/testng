package test.configuration.issue3358;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChildFirstTimeOnlyInvocationSample extends ParentFirstTimeOnlySample {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeChild() {}

  @Test(invocationCount = 3)
  public void test() {}
}
