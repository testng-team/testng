package test.bug92;

import org.testng.annotations.BeforeTest;

public class TestBase {

  static int beforeTestCount;
  static int beforeTestAlwaysCount;

  @BeforeTest
  public void baseTestBeforeTest() {
    beforeTestCount++;
  }

  @BeforeTest(alwaysRun = true)
  public void baseTestBeforeTestAlways() {
    beforeTestAlwaysCount++;
  }
}
