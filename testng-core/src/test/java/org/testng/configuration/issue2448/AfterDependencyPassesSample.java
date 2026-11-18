package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AfterDependencyPassesSample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();

  @BeforeMethod
  public void createDriver() {
    invoked.add("createDriver");
    Assert.fail("Could not create the driver");
  }

  @AfterMethod(alwaysRun = true)
  public void saveScreenshot() {
    invoked.add("saveScreenshot");
  }

  @AfterMethod(alwaysRun = true, dependsOnMethods = "saveScreenshot")
  public void uploadScreenshot() {
    invoked.add("uploadScreenshot");
  }

  @Test
  public void test() {
    invoked.add("test");
  }
}
