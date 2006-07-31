package test.dependent;

import org.testng.annotations.*;


/**
 * This class exercises dependent groups
 *
 * @author Cedric Beust, Aug 19, 2004
 * 
 */
public class SampleDependent1 {

  @Test(groups = { "fail" })
  public void fail() {
    assert false;
  }
  
  @Test(dependsOnGroups = { "fail" })
  public void shouldBeSkipped() {
  }
}
