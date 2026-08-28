package test.configuration.issue2663;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/** Middle of the three level hierarchy used by {@link InheritanceChildSample}. */
public class InheritanceParentSample extends InheritanceGrandParentSample {

  @BeforeMethod(priority = 2)
  public void parentBeforeA() {}

  @BeforeMethod(priority = 1)
  public void parentBeforeB() {}

  @AfterMethod(priority = 2)
  public void parentAfterA() {}

  @AfterMethod(priority = 1)
  public void parentAfterB() {}
}
