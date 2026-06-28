package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class DifferentClassDependsOnGroupsTest1 {
  @Test(groups = {"mainGroup"})
  public void test0() {
    assertThat(1 == 0).isTrue(); // Force a failure
  }

  @Test(dependsOnGroups = {"mainGroup"})
  public void test2() {}
}
