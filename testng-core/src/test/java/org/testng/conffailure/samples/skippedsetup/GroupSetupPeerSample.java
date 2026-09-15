package org.testng.conffailure.samples.skippedsetup;

import org.testng.annotations.Test;

/** Runs after {@link GroupSetupOwnerSample}, in the same group, and needs that group's setup. */
public class GroupSetupPeerSample {

  @Test(groups = "g")
  public void testB() {}
}
