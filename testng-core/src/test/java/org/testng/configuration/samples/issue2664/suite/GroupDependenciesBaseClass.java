package org.testng.configuration.samples.issue2664.suite;

import org.testng.annotations.BeforeSuite;

public class GroupDependenciesBaseClass {
  @BeforeSuite(groups = {"g1"})
  public void s3() {}
}
