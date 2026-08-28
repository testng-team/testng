package test.configuration.issue2663;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A dependency chain and an independent method in the same set. The independent one carries the
 * lowest priority of the three.
 */
public class DependsOnMethodsSample {

  @BeforeMethod(priority = 5)
  public void charlieBefore() {}

  @BeforeMethod(priority = 5, dependsOnMethods = "charlieBefore")
  public void alphaBefore() {}

  @BeforeMethod(priority = 1)
  public void bravoBefore() {}

  @Test
  public void test() {}
}
