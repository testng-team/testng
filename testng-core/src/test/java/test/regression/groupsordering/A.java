package test.regression.groupsordering;

import org.testng.annotations.Test;

public class A extends Base {

  @Test(groups = "a")
  public void testA() {
    s_childAWasRun = true;
  }
}
