package org.testng.listeners.samples;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigurationListenerSucceedSampleTest {

  @BeforeMethod
  public void bmShouldSucceed() {}

  @Test
  public void f() {}
}
