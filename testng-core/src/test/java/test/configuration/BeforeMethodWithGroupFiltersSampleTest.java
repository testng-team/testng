package test.configuration;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class BeforeMethodWithGroupFiltersSampleTest {

  static final String[] EXPECTED_INVOCATIONS = {
    "beforeGroup1",
    "g1m1",
    "beforeGroup1",
    "g1m2",
    "beforeGroup2",
    "g2m1",
    "beforeGroup2",
    "g2m2",
    "beforeGroup2",
    "g2m3"
  };

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
