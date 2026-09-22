package org.testng.concurrency.samples;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@Test
public class FactorySampleTest {

  @Factory
  public Object[] init() {
    return new Object[] {
      new B(), new B(),
    };
  }
}
