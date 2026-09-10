package org.testng.invocationcount.samples.issue426;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SampleTestClassWithThreadPoolSizeDefined {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethod() {}

  @Test(invocationCount = 2, threadPoolSize = 5)
  public void testMethod() {}
}
