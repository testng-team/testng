package test.commandline;

import org.testng.annotations.Test;

public class OverrideSampleTest {

  @Test
  public void f1() {}

  @Test(groups = "go")
  public void f2() {}
}
