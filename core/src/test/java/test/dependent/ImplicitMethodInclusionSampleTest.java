package test.dependent;

import org.testng.annotations.Test;

public class ImplicitMethodInclusionSampleTest {
  @Test(groups = {"linux"})
  public void a() {}

  @Test(
      groups = {"linux", "windows"},
      dependsOnMethods = {"a"})
  public void b() {}
}
