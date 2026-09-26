package org.testng.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import org.testng.configuration.samples.BeforeMethodWithGroupFiltersSampleTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

public class BeforeMethodWithGroupFiltersTest extends SimpleBaseTest {

  /** What the sample records when TestNG honours the @BeforeMethod group filters. */
  private static final String[] EXPECTED_INVOCATIONS = {
    "beforeGroup1",
    "g1m1",
    "beforeGroup1",
    "g1m2",
    "beforeGroup2",
    "g2m1",
    "beforeGroup2",
    "g2m2",
    "beforeGroup2",
    "g2m3"
  };

  @Test(description = "GITHUB-549")
  public void beforeMethodWithBeforeGroupsShouldOnlyRunBeforeGroupMethods() {
    InvokedMethodNameListener nameListener = run(BeforeMethodWithGroupFiltersSampleTest.class);
    assertThat(nameListener.getInvokedMethodNames()).containsExactly(EXPECTED_INVOCATIONS);
  }
}
