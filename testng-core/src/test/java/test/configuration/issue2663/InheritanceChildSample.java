package test.configuration.issue2663;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Three levels, each declaring two independent methods of the same kind whose names run the other
 * way round from their priorities. The inheritance guarantee decides the levels, the priority only
 * decides what happens inside one level.
 */
public class InheritanceChildSample extends InheritanceParentSample {

  @BeforeMethod(priority = 2)
  public void childBeforeA() {}

  @BeforeMethod(priority = 1)
  public void childBeforeB() {}

  @AfterMethod(priority = 2)
  public void childAfterA() {}

  @AfterMethod(priority = 1)
  public void childAfterB() {}

  @Test
  public void test() {}
}
