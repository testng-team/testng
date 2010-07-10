package test.invocationcount;

import org.testng.annotations.Test;

public class InvocationBase extends Base {
  @Test(invocationCount = 3)
  public void f() {
  }
}
