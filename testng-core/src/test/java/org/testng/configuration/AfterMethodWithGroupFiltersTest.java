package org.testng.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import org.testng.configuration.samples.AfterMethodWithGroupFiltersSampleTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class AfterMethodWithGroupFiltersTest extends SimpleBaseTest {

  /** What the sample records when TestNG honours the @AfterMethod group filters. */
  private static final String[] EXPECTED_INVOCATIONS = {
    "g1m1",
    "afterGroup1",
    "g1m2",
    "afterGroup1",
    "g2m1",
    "afterGroup2",
    "g2m2",
    "afterGroup2",
    "g2m3",
    "afterGroup2",
  };

  @Test(description = "GITHUB-549")
  public void beforeMethodWithBeforeGroupsShouldOnlyRunBeforeGroupMethods() {
    InvokedMethodNameListener nameListener = run(AfterMethodWithGroupFiltersSampleTest.class);
    assertThat(nameListener.getInvokedMethodNames()).containsExactly(EXPECTED_INVOCATIONS);
  }
}
