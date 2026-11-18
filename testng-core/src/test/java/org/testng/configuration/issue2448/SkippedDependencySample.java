package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class SkippedDependencySample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();

  @AfterMethod(alwaysRun = true)
  public void saveScreenshot() {
    invoked.add("saveScreenshot");
    throw new SkipException("No screenshot to save");
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
