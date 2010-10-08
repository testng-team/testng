package test.dependent;

import org.testng.annotations.Test;

/**
 * This class
 *
 * @author cbeust
 */
public class SampleDependentMethods5 {

  @Test
  public void step1() {
  }

  @Test(dependsOnMethods = { "step1", "blablabla" })
  public void step2() {
  }
}
