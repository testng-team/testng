package test.aftergroups.samples;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.Test;

public class AfterGroupsSample {

  @Test(groups = "group-1")
  public void someTest() {}

  @AfterGroups(groups = "group-1")
  public void afterGroupMethod() {}
}
