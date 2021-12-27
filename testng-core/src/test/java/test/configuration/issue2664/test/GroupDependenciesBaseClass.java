package test.configuration.issue2664.test;

import org.testng.annotations.BeforeTest;

public class GroupDependenciesBaseClass {
  @BeforeTest(groups = {"g1"})
  public void s3() {}
}
