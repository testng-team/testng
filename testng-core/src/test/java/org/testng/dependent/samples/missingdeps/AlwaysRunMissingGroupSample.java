package org.testng.dependent.samples.missingdeps;

import org.testng.annotations.Test;

/** Names a group no method declares, and sets {@code alwaysRun}. */
public class AlwaysRunMissingGroupSample {
  @Test(dependsOnGroups = "missing-group", alwaysRun = true)
  public void dependsOnAGroupThatIsNotThere() {}
}
