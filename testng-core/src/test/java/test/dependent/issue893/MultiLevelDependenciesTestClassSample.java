package test.dependent.issue893;

import org.testng.annotations.Test;

public class MultiLevelDependenciesTestClassSample {
  @Test
  public void grandFather() {}

  @Test(dependsOnMethods = "grandFather")
  public void father() {}

  @Test(dependsOnMethods = "grandFather")
  public void mother() {}

  @Test(dependsOnMethods = {"father", "mother"})
  public void child() {}
}
