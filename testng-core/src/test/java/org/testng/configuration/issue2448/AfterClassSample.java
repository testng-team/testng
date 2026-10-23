package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

public class AfterClassSample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();

  @AfterClass(alwaysRun = true)
  public void stopServer() {
    invoked.add("stopServer");
    Assert.fail("Could not stop the server");
  }

  @AfterClass(alwaysRun = true, dependsOnMethods = "stopServer")
  public void deleteServerData() {
    invoked.add("deleteServerData");
  }

  @Test
  public void test() {
    invoked.add("test");
  }
}
