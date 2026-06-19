package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class DifferentClassDependsOnGroupsTest1 {
  @Test(groups = {"mainGroup"})
  public void test0() {
    assertThat(1).isEqualTo(0); // Force a failure
  }

  @Test(dependsOnGroups = {"mainGroup"})
  public void test2() {}
}
