package test.reports.issue2611;

import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

public class TestClassFailsAtBeforeSuiteWithBeforeGroupsSuiteTestSample {

  @BeforeGroups(groups = {"dragon-warrior"})
  public void beforeGroups() {}

  @AfterGroups(groups = {"dragon-warrior"})
  public void afterGroups() {}

  @BeforeSuite(groups = {"dragon-warrior"})
  public void beforeSuite() {
    Assert.fail();
  }

  @AfterSuite(groups = {"dragon-warrior"})
  public void afterSuite() {}

  @BeforeTest(groups = {"dragon-warrior"})
  public void beforeTest() {}

  @AfterTest(groups = {"dragon-warrior"})
  public void afterTest() {}
}
