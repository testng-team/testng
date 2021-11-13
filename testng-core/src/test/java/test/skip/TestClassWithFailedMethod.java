package test.skip;

import org.testng.annotations.Test;

public class TestClassWithFailedMethod {

  @Test
  public void parentMethod() {
    throw new RuntimeException("simulating a failure");
  }

  @Test(dependsOnMethods = "parentMethod")
  public void childMethod() {}
}
