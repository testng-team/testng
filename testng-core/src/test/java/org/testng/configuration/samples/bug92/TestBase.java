package org.testng.configuration.samples.bug92;

import org.testng.annotations.BeforeTest;

public class TestBase {

  public static int beforeTestCount = 0;
  public static int beforeTestAlwaysCount = 0;

  @BeforeTest
  public void baseTestBeforeTest() {
    beforeTestCount++;
  }

  @BeforeTest(alwaysRun = true)
  public void baseTestBeforeTestAlways() {
    beforeTestAlwaysCount++;
  }
}
