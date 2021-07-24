package test.reports.issue2611;

import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class TestClassWithBeforeSuiteSample {

  @BeforeSuite(groups = {"dragon-warrior"})
  public void beforeSuite() {
    Assert.fail();
  }

  @AfterSuite(groups = {"dragon-warrior"})
  public void afterSuite() {}
}
