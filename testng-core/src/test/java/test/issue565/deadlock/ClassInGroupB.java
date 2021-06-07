package test.issue565.deadlock;

import org.testng.annotations.Test;

@Test(groups = "B", dependsOnGroups = "A")
public class ClassInGroupB {

  @Test
  public void groupB_1() {}

  @Test(dependsOnMethods = "groupB_1")
  public void groupB_2() {}
}
