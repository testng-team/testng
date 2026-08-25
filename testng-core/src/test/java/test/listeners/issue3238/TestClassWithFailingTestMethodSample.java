package test.listeners.issue3238;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(FailureTrackingListener.class)
public class TestClassWithFailingTestMethodSample {

  @Test
  public void failingTest() {
    fail();
  }
}
