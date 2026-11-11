package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SameClassSample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();

  @BeforeMethod(alwaysRun = true)
  public void createDriver() {
    invoked.add("createDriver");
    Assert.fail("Could not create the driver");
  }

  @BeforeMethod(alwaysRun = true, dependsOnMethods = "createDriver")
  public void openPages() {
    invoked.add("openPages");
  }

  @Test
  public void test() {
    invoked.add("test");
  }
}
