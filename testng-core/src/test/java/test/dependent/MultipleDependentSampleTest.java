package test.dependent;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class MultipleDependentSampleTest {

  @Test
  public void init() {}

  @Test(dependsOnMethods = "init")
  public void fail() {
    Assertions.fail();
  }

  @Test(dependsOnMethods = "fail")
  public void skip1() {}

  @Test(dependsOnMethods = "skip1")
  public void skip2() {}
}
