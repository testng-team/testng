package test.configuration;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class AfterMethodWithGroupFiltersSampleTest {
  static final String[] EXPECTED_INVOCATIONS = {
    "g1m1",
    "afterGroup1",
    "g1m2",
    "afterGroup1",
    "g2m1",
    "afterGroup2",
    "g2m2",
    "afterGroup2",
    "g2m3",
    "afterGroup2",
  };

  @AfterMethod(onlyForGroups = {"group1"})
  public void afterGroup1() {}

  @Test(groups = "group1")
  public void g1m1() {}

  @Test(groups = "group1")
  public void g1m2() {}

  @AfterMethod(onlyForGroups = {"group2"})
  public void afterGroup2() {}

  @Test(groups = "group2")
  public void g2m1() {}

  @Test(groups = "group2")
  public void g2m2() {}

  @Test(groups = "group2")
  public void g2m3() {}
}
