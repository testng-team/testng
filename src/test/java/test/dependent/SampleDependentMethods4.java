package test.dependent;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This class
 *
 * @author cbeust
 */
public class SampleDependentMethods4 {

  @Test
  public void step1() {
  }

  @Test(dependsOnMethods = { "step1" })
  public void step2() {
    Assert.assertTrue(false, "Problem in step2");
  }

  @Test(dependsOnMethods = { "step2" })
  public void step3() {
  }
}
