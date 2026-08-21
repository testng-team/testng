package org.testng.reporters.snapshot;

import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A configuration method skipped because an earlier one failed. It declares a parameter, so it
 * would have something to report had TestNG resolved one -- but resolving parameters for a method
 * that will not run can itself fail, and would turn this skip into a failure. It is announced with
 * nothing and reports nothing.
 *
 * <p>Its {@code @BeforeMethod} declaring a parameter is the whole point, which is why {@code
 * test.configurationfailurepolicy.ClassWithFailedBeforeClassMethod} is not reused here: its
 * configuration methods take none, so asserting that this one reports nothing would pass whatever
 * TestNG did.
 */
public class SkippedConfigurationSample {

  @BeforeClass
  public void failing() {
    throw new IllegalStateException("this configuration method fails on purpose");
  }

  @BeforeMethod
  public void skipped(ITestContext context) {}

  @Test
  public void report() {}
}
