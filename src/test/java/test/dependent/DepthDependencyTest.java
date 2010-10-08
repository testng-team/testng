package test.dependent;

import org.testng.annotations.Test;

public class DepthDependencyTest {

  @Test(groups = { "1"} )
  public void f1() {
    throw new RuntimeException();
  }

  @Test(groups = { "2"}, dependsOnGroups = {"1"} )
  public void f2() {

  }

  @Test(groups = { "3"}, dependsOnGroups = {"2"} )
  public void f3() {

  }
}
