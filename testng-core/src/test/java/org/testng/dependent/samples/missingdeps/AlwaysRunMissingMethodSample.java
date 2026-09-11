package org.testng.dependent.samples.missingdeps;

import org.testng.annotations.Test;

/** Names a method that does not exist, and sets {@code alwaysRun}. */
public class AlwaysRunMissingMethodSample {
  @Test(dependsOnMethods = "missingMethod", alwaysRun = true)
  public void dependsOnAMethodThatIsNotThere() {}
}
