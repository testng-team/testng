package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

public class AfterSuiteSample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();

  @AfterSuite(alwaysRun = true)
  public void stopGrid() {
    invoked.add("stopGrid");
    Assert.fail("Could not stop the grid");
  }

  @AfterSuite(alwaysRun = true, dependsOnMethods = "stopGrid")
  public void archiveGridLogs() {
    invoked.add("archiveGridLogs");
  }

  @Test
  public void test() {
    invoked.add("test");
  }
}
