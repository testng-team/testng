package org.testng.dependent.samples.issue2840;

import org.testng.Assert;
import org.testng.annotations.Test;

/** An unrelated class with a failing method named like {@link SameNameSample#first()}. */
public class SameNameFailingSample {

  @Test
  public void first() {
    Assert.fail("unrelated failure");
  }
}
