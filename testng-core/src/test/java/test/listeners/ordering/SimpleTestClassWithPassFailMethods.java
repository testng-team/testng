package test.listeners.ordering;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleTestClassWithPassFailMethods {

  @Test
  public void testWillPass() {}

  @Test
  public void testWillFail() {
    Assert.fail();
  }

  @Test(dependsOnMethods = "testWillFail")
  public void testWillSkip() {}
}
