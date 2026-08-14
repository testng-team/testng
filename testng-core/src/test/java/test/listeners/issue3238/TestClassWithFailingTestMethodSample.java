package test.listeners.issue3238;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(FailureTrackingListener.class)
public class TestClassWithFailingTestMethodSample {

  @Test
  public void failingTest() {
    Assert.fail();
  }
}
