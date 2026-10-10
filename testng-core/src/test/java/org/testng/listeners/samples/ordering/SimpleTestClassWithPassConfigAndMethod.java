package org.testng.listeners.samples.ordering;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SimpleTestClassWithPassConfigAndMethod {
  @BeforeClass
  public void beforeClass() {}

  @Test
  public void testWillPass() {}
}
