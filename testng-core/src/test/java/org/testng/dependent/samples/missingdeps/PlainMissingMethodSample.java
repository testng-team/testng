package org.testng.dependent.samples.missingdeps;

import org.testng.annotations.Test;

/** Names a method that does not exist, and asks for no leniency. */
public class PlainMissingMethodSample {
  @Test(dependsOnMethods = "missingMethod")
  public void dependsOnAMethodThatIsNotThere() {}
}
