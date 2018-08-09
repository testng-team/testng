package test.listeners.ordering;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SimpleTestClassWithFailedAndSkippedConfigAndSkippedTestMethod {
  @BeforeClass
  public void beforeClass() {
    Assert.fail();
  }

  @BeforeMethod
  public void beforeMethod() {}

  @Test
  public void testWillPass() {}

}
