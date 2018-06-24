package test.dependent.github1156;

import org.testng.annotations.Test;

public class ASample {

  @Test(dependsOnMethods = "test.dependent.github1156.BSample.testB")
  public void testA() {}
}
