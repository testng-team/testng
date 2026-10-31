package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/** The teardown dependency fails in the first invocation only. */
public class RepeatedInvocationSample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();
  static final AtomicInteger saves = new AtomicInteger();

  @AfterMethod(alwaysRun = true)
  public void saveScreenshot() {
    invoked.add("saveScreenshot");
    if (saves.incrementAndGet() == 1) {
      Assert.fail("Could not save the first screenshot");
    }
  }

  @AfterMethod(alwaysRun = true, dependsOnMethods = "saveScreenshot")
  public void uploadScreenshot() {
    invoked.add("uploadScreenshot");
  }

  @Test(invocationCount = 2)
  public void test() {
    invoked.add("test");
  }
}
