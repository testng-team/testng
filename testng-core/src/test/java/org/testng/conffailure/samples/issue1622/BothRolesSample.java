package org.testng.conffailure.samples.issue1622;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * One Java method in two roles. TestNG merges every configuration annotation on a method into one
 * object, so {@code alwaysRun} on the after role must not leak into the before role.
 */
public class BothRolesSample {

  @BeforeClass
  public void failingBeforeClass() {
    throw new RuntimeException("Simulating a failure in @BeforeClass");
  }

  @BeforeMethod
  @AfterMethod(alwaysRun = true)
  public void both() {}

  @Test
  public void testMethod() {}
}
