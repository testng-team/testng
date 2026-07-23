package test.listeners.ordering;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SimpleTestClassWithFailedAndSkippedConfigAndSkippedTestMethod {
  @BeforeClass
  public void beforeClass() {
    fail();
  }

  @BeforeMethod
  public void beforeMethod() {}

  @Test
  public void testWillPass() {}
}
