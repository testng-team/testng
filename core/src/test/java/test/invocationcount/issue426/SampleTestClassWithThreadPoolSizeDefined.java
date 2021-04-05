package test.invocationcount.issue426;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SampleTestClassWithThreadPoolSizeDefined {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethod() {}

  @Test(invocationCount = 2)
  public void testMethod() {}
}
