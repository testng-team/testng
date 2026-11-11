package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class AfterSample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();

  @AfterMethod(alwaysRun = true)
  public void saveScreenshot() {
    invoked.add("saveScreenshot");
    Assert.fail("Could not save the screenshot");
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
