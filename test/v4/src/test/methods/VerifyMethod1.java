package test.methods;

import org.testng.annotations.*;

/**
 * This class verifies that the correct methods were run
 * 
 * @author cbeust
 */
@Test(dependsOnGroups = { "sample1" })
public class VerifyMethod1 {
  
  @Configuration(beforeSuite = true)
  public void init() {
    SampleMethod1.reset();
  }
  
  @Test
  public void verify() {
    SampleMethod1.verify();
  }

}
