package test.configuration.issue2663;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Two independent methods of the same kind in the same class. The names are deliberately ordered
 * the other way round from the priorities, so the assertion cannot be satisfied by the alphabetical
 * tie-break that applies when no priority is given.
 */
public class SameClassSample {

  @BeforeMethod(priority = 2)
  public void alphaBefore() {}

  @BeforeMethod(priority = 1)
  public void bravoBefore() {}

  @AfterMethod(priority = 2)
  public void alphaAfter() {}

  @AfterMethod(priority = 1)
  public void bravoAfter() {}

  @Test
  public void test() {}
}
