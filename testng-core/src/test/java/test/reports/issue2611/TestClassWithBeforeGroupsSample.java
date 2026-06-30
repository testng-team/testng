package test.reports.issue2611;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;

public class TestClassWithBeforeGroupsSample {

  @BeforeGroups(groups = {"dragon-warrior"})
  public void beforeGroups() {
    fail();
  }

  @AfterGroups(groups = {"dragon-warrior"})
  public void afterGroups() {}
}
