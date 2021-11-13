package test.reports;

import org.testng.annotations.Test;

public class Issue1659Sample {
  @Test
  public void testMethod() {
    triggerExceptions();
  }

  private void triggerExceptions() {
    throw new RuntimeException("simulating test failure");
  }
}
