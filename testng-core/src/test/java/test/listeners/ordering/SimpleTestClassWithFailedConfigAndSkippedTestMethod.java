package test.listeners.ordering;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SimpleTestClassWithFailedConfigAndSkippedTestMethod {
  @BeforeClass
  public void beforeClass() {
    fail();
  }

  @Test
  public void testWillPass() {}
}
