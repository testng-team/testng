package org.testng.invocationcount.samples;

import org.testng.annotations.Test;

public class InvocationBase {
  @Test(invocationCount = 3)
  public void f() {}
}
