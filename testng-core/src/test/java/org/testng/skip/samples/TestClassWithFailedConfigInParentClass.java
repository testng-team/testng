package org.testng.skip.samples;

import org.testng.annotations.Test;

public class TestClassWithFailedConfigInParentClass extends TestClassWithFailedConfig {

  @Test
  public void testMethodInChildClass() {}
}
