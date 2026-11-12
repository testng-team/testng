package org.testng.dependent.samples.issue3201;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/** Four instances that each record the order in which their dependent methods run. */
public class ChainedFactorySample {

  public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

  private final int index;

  public ChainedFactorySample(int index) {
    this.index = index;
  }

  @Factory
  public static Object[] instances() {
    return new Object[] {
      new ChainedFactorySample(0),
      new ChainedFactorySample(1),
      new ChainedFactorySample(2),
      new ChainedFactorySample(3)
    };
  }

  @Test
  public void a() {
    record("a");
  }

  @Test(dependsOnMethods = "a")
  public void b() {
    record("b");
  }

  @Test(dependsOnMethods = "b")
  public void c() {
    record("c");
  }

  @Test(dependsOnMethods = "c")
  public void d() {
    record("d");
  }

  private void record(String method) {
    // A method that finishes late gives a method that wrongly started with it time to record first.
    if (!method.equals("d")) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    EVENTS.add(index + "." + method);
  }
}
