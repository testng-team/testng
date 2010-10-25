package test.dependent;

import org.testng.annotations.Test;

/**
 * This class
 *
 * @author cbeust
 */
public class SampleDependentMethods6 {
  @Test(dependsOnMethods = { "step2" })
  public void step1() {
  }

  @Test(dependsOnMethods = { "step1" })
  public void step2() {
  }
}
