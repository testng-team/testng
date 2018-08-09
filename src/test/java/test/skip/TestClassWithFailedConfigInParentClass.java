package test.skip;

import org.testng.annotations.Test;

public class TestClassWithFailedConfigInParentClass extends TestClassWithFailedConfig {

  @Test
  public void testMethodInChildClass() {
  }

}
