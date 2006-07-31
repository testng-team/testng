package test.dependent;

import org.testng.annotations.*;

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
    assert false : "Problem in step2";
  }

  @Test(dependsOnMethods = { "step2" })
  public void step3() {
  }
}
