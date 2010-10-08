package test.dependent;

import org.testng.annotations.Test;

public class ImplicitGroupInclusionSampleTest {

  @Test(groups = "z")
  public void z() {

  }

  @Test(groups = "a", dependsOnGroups = {"z"})
  public void a() {

  }

  @Test(groups = "b", dependsOnGroups = {"a"})
  public void b() {

  }
}
