package test.skip;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestClassWithFailedConfig {

  @BeforeClass
  public void beforeClass() {
    throw new RuntimeException("simulating a failure");
  }

  @Test
  public void testMethod() {}
}
