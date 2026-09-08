package test.reports;

import org.testng.ITest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class ITestFailingConfigSample implements ITest {

  @Test
  public void run() {}

  @AfterMethod
  public void tearDown() {
    throw new RuntimeException("simulated config failure");
  }

  @Override
  public String getTestName() {
    return "custom";
  }
}
