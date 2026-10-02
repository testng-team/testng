package org.testng.listeners.samples.ordering;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class SimpleTestClassWithFailedMethodMultipleInvocations {

  @Test(invocationCount = 2)
  public void testWillFail() {
    fail();
  }
}
