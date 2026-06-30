package test.reports.issue2611;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class TestClassWithBeforeSuiteSample {

  @BeforeSuite(groups = {"dragon-warrior"})
  public void beforeSuite() {
    fail();
  }

  @AfterSuite(groups = {"dragon-warrior"})
  public void afterSuite() {}
}
