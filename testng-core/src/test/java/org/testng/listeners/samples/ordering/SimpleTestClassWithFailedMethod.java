package org.testng.listeners.samples.ordering;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class SimpleTestClassWithFailedMethod {

  @Test
  public void testWillFail() {
    fail();
  }
}
