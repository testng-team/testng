package test.invocationcount;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class DataProviderFalseTrueTest extends DataProviderBase {
  @BeforeMethod()
  public void beforeMethod() {}

  @AfterMethod(lastTimeOnly = true)
  public void afterMethod() {}
}
