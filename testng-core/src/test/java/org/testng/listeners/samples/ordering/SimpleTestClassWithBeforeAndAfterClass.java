package org.testng.listeners.samples.ordering;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SimpleTestClassWithBeforeAndAfterClass {
  @BeforeClass
  public void beforeClass() {}

  @Test
  public void testWillPass() {}

  @AfterClass
  public void afterClass() {}
}
