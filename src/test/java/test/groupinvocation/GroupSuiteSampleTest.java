package test.groupinvocation;

import org.testng.annotations.Test;

public class GroupSuiteSampleTest {

  @Test(groups = "a")
  public void a() {}

  @Test(groups = "b")
  public void b() {}

  @Test
  public void c() {}
}

