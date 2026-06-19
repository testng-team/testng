package test.invocationcount.issue426;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SampleTestClassWithThreadPoolAndFirstLastTimeConfigs {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethod() {}

  @Test(invocationCount = 2, threadPoolSize = 5)
  public void testMethod() {}

  @AfterMethod(lastTimeOnly = true)
  public void afterMethod() {}
}
