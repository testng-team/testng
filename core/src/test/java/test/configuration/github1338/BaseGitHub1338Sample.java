package test.configuration.github1338;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;

public class BaseGitHub1338Sample {

  private String someObject = null;

  @BeforeClass(alwaysRun = true)
  public void classSetUp() {
    someObject = "not null";
  }

  @BeforeGroups(
      groups = {"group1"},
      alwaysRun = true)
  public void groupSetUp() {
    if (someObject == null) {
      throw new NullPointerException("someObject is null");
    }
  }
}
