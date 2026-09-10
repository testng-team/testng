package org.testng.invocationcount.samples;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class DataProviderTrueTrueTest extends DataProviderBase {
  @BeforeMethod(firstTimeOnly = true)
  public void beforeMethod() {}

  @AfterMethod(lastTimeOnly = true)
  public void afterMethod() {}
}
