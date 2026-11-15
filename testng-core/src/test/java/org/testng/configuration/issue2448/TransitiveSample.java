package org.testng.configuration.issue2448;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

/** A fails, B depends on A, C depends on B. */
public class TransitiveSample {
  static final List<String> invoked = new CopyOnWriteArrayList<>();

  @AfterMethod(alwaysRun = true)
  public void a() {
    invoked.add("a");
    Assert.fail("a fails");
  }

  @AfterMethod(alwaysRun = true, dependsOnMethods = "a")
  public void b() {
    invoked.add("b");
  }

  @AfterMethod(alwaysRun = true, dependsOnMethods = "b")
  public void c() {
    invoked.add("c");
  }

  @Test
  public void test() {
    invoked.add("test");
  }
}
