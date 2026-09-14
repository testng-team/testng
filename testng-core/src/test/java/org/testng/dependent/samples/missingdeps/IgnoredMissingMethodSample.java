package org.testng.dependent.samples.missingdeps;

import org.testng.annotations.Test;

/** Names a method that does not exist, and sets {@code ignoreMissingDependencies}. */
public class IgnoredMissingMethodSample {
  @Test(dependsOnMethods = "missingMethod", ignoreMissingDependencies = true)
  public void dependsOnAMethodThatIsNotThere() {}
}
