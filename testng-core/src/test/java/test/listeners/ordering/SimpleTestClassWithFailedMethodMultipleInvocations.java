package test.listeners.ordering;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleTestClassWithFailedMethodMultipleInvocations {

  @Test(invocationCount = 2)
  public void testWillFail() {
    Assert.fail();
  }
}
