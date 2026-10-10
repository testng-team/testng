package org.testng.configuration.samples;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class AfterMethodWithGroupFiltersSampleTest {
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
