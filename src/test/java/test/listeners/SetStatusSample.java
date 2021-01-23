package test.listeners;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SetStatusSample {

  @Test
  public void aFailingTest() {
    Assert.fail("Failing deliberately");
  }
}
