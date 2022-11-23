package test.dependent.issue141;

import org.testng.annotations.Test;

public class TestClassSample {

  @Test(dependsOnMethods = "test_C[0-9]{7}")
  public void randomTest() {}

  @Test
  public void test_C6390323() {}
}
