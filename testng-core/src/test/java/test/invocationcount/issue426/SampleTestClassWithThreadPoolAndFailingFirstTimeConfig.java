package test.invocationcount.issue426;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SampleTestClassWithThreadPoolAndFailingFirstTimeConfig {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethod() {
    throw new RuntimeException("firstTimeOnly setup failed");
  }

  @Test(invocationCount = 2, threadPoolSize = 5)
  public void testMethod() {}
}
