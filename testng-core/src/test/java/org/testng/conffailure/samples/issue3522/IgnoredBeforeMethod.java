package org.testng.conffailure.samples.issue3522;

import org.testng.annotations.BeforeMethod;

public interface IgnoredBeforeMethod {

  @BeforeMethod(ignoreFailure = true)
  default void ifaceSetup() {
    throw new RuntimeException("iface setup fails");
  }
}
