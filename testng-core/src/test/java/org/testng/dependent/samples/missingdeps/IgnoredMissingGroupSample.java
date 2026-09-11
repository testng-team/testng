package org.testng.dependent.samples.missingdeps;

import org.testng.annotations.Test;

/** Names a group no method declares, and sets {@code ignoreMissingDependencies}. */
public class IgnoredMissingGroupSample {
  @Test(dependsOnGroups = "missing-group", ignoreMissingDependencies = true)
  public void dependsOnAGroupThatIsNotThere() {}
}
