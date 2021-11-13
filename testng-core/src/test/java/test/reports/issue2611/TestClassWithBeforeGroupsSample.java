package test.reports.issue2611;

import org.testng.Assert;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

public class TestClassWithBeforeGroupsSample {

  @BeforeGroups(groups = {"dragon-warrior"})
  public void beforeGroups() {
    Assert.fail();
  }

  @AfterGroups(groups = {"dragon-warrior"})
  public void afterGroups() {}
}
