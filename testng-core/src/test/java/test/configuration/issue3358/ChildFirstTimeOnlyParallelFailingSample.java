package test.configuration.issue3358;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChildFirstTimeOnlyParallelFailingSample extends ParentFirstTimeOnlySample {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeChild() {
    throw new RuntimeException("child firstTimeOnly failed");
  }

  @Test(invocationCount = 3, threadPoolSize = 3)
  public void test() {}
}
