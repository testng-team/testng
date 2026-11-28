package org.testng.conffailure.samples.issue3627;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

/**
 * Two equal instances. The {@code @BeforeMethod} method of the first one fails, and the one of the
 * second one skips.
 */
public class FailingAndSkippingSetupsSample extends EqualInstancesSample {

  @Factory(dataProvider = "instances")
  public FailingAndSkippingSetupsSample(String name, boolean skip) {
    super(name, skip);
  }

  // Both instances have the same toString(), so TestNG orders them by their factory parameters and
  // the failing instance runs first.
  @DataProvider
  public static Object[][] instances() {
    return new Object[][] {{"failing", false}, {"skipping", true}};
  }
}
