package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class AfterTestSample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();

  @AfterTest(alwaysRun = true)
  public void closeDatabase() {
    invoked.add("closeDatabase");
    Assert.fail("Could not close the database");
  }

  @AfterTest(alwaysRun = true, dependsOnMethods = "closeDatabase")
  public void dropDatabase() {
    invoked.add("dropDatabase");
  }

  @Test
  public void test() {
    invoked.add("test");
  }
}
