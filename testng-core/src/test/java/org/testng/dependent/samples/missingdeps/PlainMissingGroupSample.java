package org.testng.dependent.samples.missingdeps;

import org.testng.annotations.Test;

/** Names a group no method declares, and asks for no leniency. */
public class PlainMissingGroupSample {
  @Test(dependsOnGroups = "missing-group")
  public void dependsOnAGroupThatIsNotThere() {}
}
