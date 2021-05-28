package test.groupinvocation;

import org.testng.annotations.Test;

public class GroupSuiteSampleTest2 {

  @Test(groups = "a")
  public void a2() {}

  @Test(groups = "b")
  public void b2() {}

  @Test
  public void c2() {}
}
