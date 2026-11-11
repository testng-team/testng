package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;

public class BaseSample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();

  @BeforeMethod(alwaysRun = true)
  public void createDriver() {
    invoked.add("createDriver");
    Assert.fail("Could not create the driver");
  }
}
