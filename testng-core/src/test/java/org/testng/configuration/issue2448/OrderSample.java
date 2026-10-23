package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** Method names sort against the declared dependencies, so only dependsOnMethods sets the order. */
public class OrderSample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();

  @BeforeMethod(alwaysRun = true, dependsOnMethods = "zCreateDriver")
  public void aOpenPages() {
    invoked.add("aOpenPages");
  }

  @BeforeMethod(alwaysRun = true)
  public void zCreateDriver() {
    invoked.add("zCreateDriver");
  }

  @AfterMethod(alwaysRun = true, dependsOnMethods = "zSaveScreenshot")
  public void aUploadScreenshot() {
    invoked.add("aUploadScreenshot");
  }

  @AfterMethod(alwaysRun = true)
  public void zSaveScreenshot() {
    invoked.add("zSaveScreenshot");
  }

  @Test
  public void test() {
    invoked.add("test");
  }
}
