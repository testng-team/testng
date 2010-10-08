package test.dependent;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * This class exercises dependent groups
 *
 * @author Cedric Beust, Aug 19, 2004
 *
 */
public class SampleDependent1 {

  @Test(groups = { "fail" })
  public void fail() {
    Assert.assertTrue(false);
  }

  @Test(dependsOnGroups = { "fail" })
  public void shouldBeSkipped() {
  }
}
