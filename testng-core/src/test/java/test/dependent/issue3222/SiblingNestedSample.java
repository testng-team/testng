package test.dependent.issue3222;

import org.testng.annotations.Test;

public class SiblingNestedSample {

  public static class First {
    @Test
    public void test1() {}
  }

  public static class Second {
    @Test(dependsOnMethods = "test1")
    public void test2() {}
  }
}
