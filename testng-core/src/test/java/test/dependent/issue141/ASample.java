package test.dependent.issue141;

import org.testng.annotations.Test;

public class ASample {
  @Test(dependsOnMethods = "test.dependent.issue141.BSample.b*")
  public void a() {}
}
