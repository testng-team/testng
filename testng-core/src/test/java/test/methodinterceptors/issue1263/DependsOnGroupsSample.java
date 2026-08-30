package test.methodinterceptors.issue1263;

import org.testng.annotations.Test;

public class DependsOnGroupsSample {

  @Test(groups = "free")
  public void independent() {}

  @Test(groups = "prereq")
  public void prerequisite() {}

  @Test(dependsOnGroups = "prereq")
  public void dependent() {}
}
