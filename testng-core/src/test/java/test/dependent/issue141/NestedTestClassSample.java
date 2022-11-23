package test.dependent.issue141;

import org.testng.Assert;
import org.testng.annotations.Test;

public class NestedTestClassSample {

  public static class FirstSample {
    @Test(dependsOnMethods = "test_C[0-9]{7}")
    public void randomTest() {}

    @Test
    public void test_C6390323() {}
  }

  public static class SecondSample {
    @Test(dependsOnMethods = "test_C[0-9]{7}")
    public void randomTest() {}

    @Test
    public void test_C6390323() {
      Assert.fail();
    }
  }
}
