package test.configuration.issue2663;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/** Top of the three level hierarchy used by {@link InheritanceChildSample}. */
public class InheritanceGrandParentSample {

  @BeforeMethod(priority = 2)
  public void grandParentBeforeA() {}

  @BeforeMethod(priority = 1)
  public void grandParentBeforeB() {}

  @AfterMethod(priority = 2)
  public void grandParentAfterA() {}

  @AfterMethod(priority = 1)
  public void grandParentAfterB() {}
}
