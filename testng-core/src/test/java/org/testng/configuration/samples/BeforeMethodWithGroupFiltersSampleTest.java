package org.testng.configuration.samples;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BeforeMethodWithGroupFiltersSampleTest {
  @BeforeMethod(onlyForGroups = {"group1"})
  public void beforeGroup1() {}

  @Test(groups = "group1")
  public void g1m1() {}

  @Test(groups = "group1")
  public void g1m2() {}

  @BeforeMethod(onlyForGroups = {"group2"})
  public void beforeGroup2() {}

  @Test(groups = "group2")
  public void g2m1() {}

  @Test(groups = "group2")
  public void g2m2() {}

  @Test(groups = "group2")
  public void g2m3() {}
}
