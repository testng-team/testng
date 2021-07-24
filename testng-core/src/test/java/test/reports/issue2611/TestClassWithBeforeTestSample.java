package test.reports.issue2611;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

public class TestClassWithBeforeTestSample {

  @BeforeTest(groups = {"dragon-warrior"})
  public void beforeTest() {
    Assert.fail();
  }

  @AfterTest(groups = {"dragon-warrior"})
  public void afterTest() {}
}
