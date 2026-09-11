package org.testng.factory.samples;

import org.testng.annotations.Factory;

public class DisabledFactory {

  @Factory(enabled = false)
  public Object[] factory() {
    return new Object[] {new MySample()};
  }
}
