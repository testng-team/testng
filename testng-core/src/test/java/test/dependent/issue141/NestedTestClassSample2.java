package test.dependent.issue141;

import org.testng.annotations.Test;

public class NestedTestClassSample2 {

  @Test(dependsOnMethods = "test_C[0-9]{7}")
  public void randomTest() {}

  public static class InnerClass {

    @Test
    public void test_C6390323() {}
  }
}
