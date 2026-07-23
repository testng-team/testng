package test.listeners.ordering;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class SimpleTestClassWithPassFailMethods {

  @Test
  public void testWillPass() {}

  @Test
  public void testWillFail() {
    fail();
  }

  @Test(dependsOnMethods = "testWillFail")
  public void testWillSkip() {}
}
