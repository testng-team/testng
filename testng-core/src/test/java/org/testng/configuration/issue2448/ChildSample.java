package org.testng.configuration.issue2448;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ChildSample extends BaseSample {

  @BeforeMethod(alwaysRun = true, dependsOnMethods = "createDriver")
  public void openPages() {
    invoked.add("openPages");
  }

  @Test
  public void test() {
    invoked.add("test");
  }
}
