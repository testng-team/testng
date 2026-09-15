package org.testng.conffailure.samples.retry;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Its own setup runs after the parent one; on the retry the parent one fails first. */
public class ChildSetupSample extends ParentSetupFailsOnRetrySample {

  @BeforeMethod
  public void childSetup() {}

  @Test(retryAnalyzer = RetryOnce.class)
  public void flaky() {
    throw new AssertionError("always fails");
  }
}
