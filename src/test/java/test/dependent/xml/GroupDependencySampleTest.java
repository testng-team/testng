package test.dependent.xml;

import org.testng.annotations.Test;

public class GroupDependencySampleTest {

  @Test(groups = "a")
  public void a1() {}

  @Test(groups = "b")
  public void b1() {}

  @Test(groups = "c")
  public void c1() {}
}
