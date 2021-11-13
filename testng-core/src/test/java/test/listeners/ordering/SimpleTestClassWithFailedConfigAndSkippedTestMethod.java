package test.listeners.ordering;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SimpleTestClassWithFailedConfigAndSkippedTestMethod {
  @BeforeClass
  public void beforeClass() {
    Assert.fail();
  }

  @Test
  public void testWillPass() {}
}
