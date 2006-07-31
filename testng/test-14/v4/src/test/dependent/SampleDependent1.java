package test.dependent;




/**
 * This class exercises dependent groups
 *
 * @author Cedric Beust, Aug 19, 2004
 * 
 */
public class SampleDependent1 {

  /**
   * @testng.test groups="current,fail"
   */
  public void fail() {
    assert false;
  }

    /**
     * @testng.test dependsOnGroups="fail"
     */
  public void shouldBeSkipped() {
  }
}
