package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * The same nothing to say, on the other kind of row: a {@code @Factory} parameter whose {@code
 * toString()} answers {@code null}.
 *
 * <p>No snapshot describes a factory parameter, so it is still read from the result -- but what a
 * report writes for a value with no rendering is the same question either way.
 */
public class NoRenderingFactoryParameterSample {

  @DataProvider(name = "speechless")
  public static Object[][] speechless() {
    return new Object[][] {{new NoRenderingParameterSample.Speechless()}};
  }

  @Factory(dataProvider = "speechless")
  public NoRenderingFactoryParameterSample(NoRenderingParameterSample.Speechless nothingToSay) {}

  @Test
  public void report() {}
}
