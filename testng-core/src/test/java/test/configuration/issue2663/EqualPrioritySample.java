package test.configuration.issue2663;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Equal priorities leave the existing deterministic tie-break in charge. */
public class EqualPrioritySample {

  @BeforeMethod(priority = 5)
  public void bravoBefore() {}

  @BeforeMethod(priority = 5)
  public void alphaBefore() {}

  @Test
  public void test() {}
}
