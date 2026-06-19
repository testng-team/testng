package test.failedreporter.issue1297.depend_on_test;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class PassDependsOnPassSample {
  @Test
  public void test1() {}

  @Test(dependsOnMethods = "test1")
  public void test2() {
    fail();
  }

  @Test(dependsOnMethods = "test1")
  public void dependsOnTest1() {}

  @Test(dependsOnMethods = "test2")
  public void dependsOnTest2() {}
}
