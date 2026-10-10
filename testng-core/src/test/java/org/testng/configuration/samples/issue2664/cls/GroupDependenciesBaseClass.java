package org.testng.configuration.samples.issue2664.cls;

import org.testng.annotations.BeforeClass;

public class GroupDependenciesBaseClass {
  @BeforeClass(groups = {"g1"})
  public void s3() {}
}
