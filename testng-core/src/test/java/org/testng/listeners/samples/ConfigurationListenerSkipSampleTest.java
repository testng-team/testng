package org.testng.listeners.samples;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigurationListenerSkipSampleTest {

  @BeforeMethod
  public void bmShouldFail() {
    throw new RuntimeException();
  }

  @BeforeMethod(dependsOnMethods = "bmShouldFail")
  public void bm() {}

  @Test
  public void f() {}
}
