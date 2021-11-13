package test.priority.issue2137;

import org.testng.annotations.Test;

public class IssueTest {

  @Test(priority = Integer.MIN_VALUE)
  public void a() {}

  @Test(priority = 1)
  public void b() {}

  @Test(priority = 2)
  public void c() {}

  @Test(priority = 3)
  public void d() {}

  @Test(priority = 4)
  public void e() {}

  @Test(priority = 5)
  public void f() {}

  @Test(priority = 6)
  public void g() {}

  @Test(priority = 7)
  public void h() {}

  @Test(priority = 8)
  public void i() {}

  @Test(priority = 9)
  public void j() {}

  @Test(priority = 10)
  public void k() {}

  @Test(priority = 11)
  public void l() {}

  @Test(priority = 12)
  public void m() {}

  @Test(priority = Integer.MAX_VALUE)
  public void z() {}
}
