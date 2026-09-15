package org.testng.conffailure.samples.issue1622;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Fails one level down from {@link FailingBeforeSuiteSample}: a suite failure short-circuits the
 * failure check before it reads the policy, a class failure does not, so this is the sample that
 * reaches the {@code CONTINUE} paths.
 */
public class FailingBeforeClassSample {

  @BeforeClass
  public void failingBeforeClass() {
    throw new RuntimeException("Simulating a failure in @BeforeClass");
  }

  @BeforeMethod(alwaysRun = true)
  public void beforeMethod() {}

  @Test
  public void testMethod() {}

  @AfterMethod(alwaysRun = true)
  public void afterMethod() {}
}
