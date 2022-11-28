package test.dependent.issue893;

import org.testng.annotations.Test;

public class TestClassSample {

  @Test
  public void independentTest() {}

  @Test(dependsOnMethods = "independentTest")
  public void dependentTest() {}

  @Test(dependsOnMethods = "independentTest")
  public void anotherDependentTest() {}
}
