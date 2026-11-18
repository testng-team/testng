package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class DisabledDependencySample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();

  @AfterMethod(enabled = false)
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
