package test.invocationcount.issue426;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SampleTestClassWithNoThreadPoolSizeDefined {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethod() {}

  @Test(invocationCount = 2, threadPoolSize = 5)
  public void testMethod() {}
}
