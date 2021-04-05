package test.dependent;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MultipleDependentSampleTest {

  @Test
  public void init() {}

  @Test(dependsOnMethods = "init")
  public void fail() {
    Assert.fail();
  }

  @Test(dependsOnMethods = "fail")
  public void skip1() {}

  @Test(dependsOnMethods = "skip1")
  public void skip2() {}
}
