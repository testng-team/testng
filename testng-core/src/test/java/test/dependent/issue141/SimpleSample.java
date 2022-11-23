package test.dependent.issue141;

import org.testng.annotations.Test;

public class SimpleSample {

  @Test(dependsOnMethods = "test.dependent.issue141.BSample.xx*")
  public void testMethod() {}
}
