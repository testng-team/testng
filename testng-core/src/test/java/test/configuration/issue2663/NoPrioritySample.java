package test.configuration.issue2663;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * The same shape as {@link SameClassSample} with no priority at all: the ordering must not move.
 */
public class NoPrioritySample {

  @BeforeMethod
  public void alphaBefore() {}

  @BeforeMethod
  public void bravoBefore() {}

  @AfterMethod
  public void alphaAfter() {}

  @AfterMethod
  public void bravoAfter() {}

  @Test
  public void test() {}
}
