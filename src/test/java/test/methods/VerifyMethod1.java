package test.methods;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * This class verifies that the correct methods were run
 *
 * @author cbeust
 */
@Test(dependsOnGroups = { "sample1" })
public class VerifyMethod1 {

  @BeforeSuite
  public void init() {
    SampleMethod1.reset();
  }

  @Test
  public void verify() {
    SampleMethod1.verify();
  }

}
