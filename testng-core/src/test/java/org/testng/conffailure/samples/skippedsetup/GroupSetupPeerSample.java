package org.testng.conffailure.samples.skippedsetup;

import org.testng.annotations.Test;

/** Runs after {@link GroupSetupOwnerSample}, in the same group, with no failure of its own. */
public class GroupSetupPeerSample {

  @Test(groups = "g")
  public void testB() {}
}
