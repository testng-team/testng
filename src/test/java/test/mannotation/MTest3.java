package test.mannotation;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test(groups = "child-class-test3", dependsOnGroups = "dog1", dependsOnMethods = "dom1")
public class MTest3 extends MBase {

  @Test(groups = "method-test3")
  public void groups1() {}

  @Test
  public void groups2() {}

  @Test(dependsOnGroups = "dog2")
  public void dependsOnGroups1() {}

  @Test
  public void dependsOnGroups2() {}

  @Test(dependsOnMethods = "dom2")
  public void dependsOnMethods1() {}

  @Test
  public void dependsOnMethods2() {}

  @Test(enabled = false)
  public void enabled1() {}

  @Test
  public void enabled2() {}

  @Configuration(beforeSuite = true, groups = "method-test3")
  public void beforeSuite() {
  }

}
