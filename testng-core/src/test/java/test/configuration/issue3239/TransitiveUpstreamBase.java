package test.configuration.issue3239;

import org.testng.annotations.BeforeClass;

class TransitiveUpstreamBase {

  @BeforeClass(groups = "g")
  protected final void baseGroup() {}

  @BeforeClass(dependsOnGroups = "g")
  protected final void baseAfterGroup() {}
}
