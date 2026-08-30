package org.testng.methodinterceptors.samples;

import org.testng.annotations.Test;

/** No method belongs to {@code nightly}, which only the run that schedules {@code heavy} minds. */
public class MissingGroupSample {

  @Test(dependsOnGroups = "nightly")
  public void heavy() {}

  @Test
  public void light() {}
}
