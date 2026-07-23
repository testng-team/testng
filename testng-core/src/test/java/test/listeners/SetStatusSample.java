package test.listeners;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

public class SetStatusSample {

  @Test
  public void aFailingTest() {
    fail("Failing deliberately");
  }
}
