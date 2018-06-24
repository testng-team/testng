package org.testng;

import static org.assertj.core.api.Assertions.assertThat;
import org.testng.annotations.Test;
import org.testng.internal.RuntimeBehavior;
import test.SimpleBaseTest;

public class DryRunTest extends SimpleBaseTest {

  @Test
  public void testDryRun() {
    System.setProperty(RuntimeBehavior.TESTNG_MODE_DRYRUN, "true");
    try {
      TestNG tng = create(DryRunSample.class);
      TestListenerAdapter listener = new TestListenerAdapter();
      tng.addListener(listener);
      tng.run();
      assertThat(listener.getPassedTests()).hasSize(2);
    } finally {
      System.setProperty(RuntimeBehavior.TESTNG_MODE_DRYRUN, "false");
    }
  }
}
