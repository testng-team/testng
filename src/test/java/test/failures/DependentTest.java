package test.failures;

import org.testng.annotations.Test;

public class DependentTest {

  @Test
  public void f1() {

  }

  @Test(dependsOnMethods = {"f1"}, dependsOnGroups = { "f" })
  public void f2() {
    throw new RuntimeException();
  }
}
