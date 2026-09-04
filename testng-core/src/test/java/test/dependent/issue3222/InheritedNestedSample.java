package test.dependent.issue3222;

import org.testng.annotations.Test;

public class InheritedNestedSample {

  public static class NestedBase {
    @Test
    public void test1() {}
  }

  public static class NestedChild extends NestedBase {
    @Test(dependsOnMethods = "test1")
    public void test2() {}
  }
}
