package org.testng.dependent.samples.issue2840;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Lazy;
import org.testng.annotations.Test;

/** A lazy factory whose instances record when they are created and when their methods run. */
public class LazyFactorySample {

  public static final List<String> EVENTS = new CopyOnWriteArrayList<>();

  private final int index;

  @Factory(dataProvider = "indices", lazy = Lazy.TRUE)
  public LazyFactorySample(int index) {
    this.index = index;
    EVENTS.add("create " + index);
  }

  @DataProvider
  public static Object[][] indices() {
    return new Object[][] {{0}, {1}};
  }

  @Test
  public void first() {
    EVENTS.add("first " + index);
  }

  @Test(dependsOnMethods = "first")
  public void second() {
    EVENTS.add("second " + index);
  }
}
