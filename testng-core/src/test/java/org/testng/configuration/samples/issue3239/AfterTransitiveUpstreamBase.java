package org.testng.configuration.samples.issue3239;

import org.testng.annotations.AfterClass;

class AfterTransitiveUpstreamBase {

  @AfterClass(groups = "g")
  protected final void parentAfter() {}
}
