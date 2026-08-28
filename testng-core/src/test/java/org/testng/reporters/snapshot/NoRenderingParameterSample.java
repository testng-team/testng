package org.testng.reporters.snapshot;

import org.jspecify.annotations.Nullable;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A parameter with nothing to say: its {@code toString()} answers {@code null}, which is not the
 * same as not being there.
 *
 * <p>Not {@link UnrenderableParameterSample}, which is the neighbouring case and reads alike: that
 * one's {@code toString()} throws, and {@code Utils.toString(Object)} guards it. Nothing throws
 * here, so there is nothing for it to guard -- it answers the {@code null} back.
 *
 * <p>{@code Utils.escapeHtml} does not accept one, so a report handing the first straight to the
 * second was lost whole to a {@code NullPointerException} that a single such parameter was enough
 * to cause.
 */
public class NoRenderingParameterSample {

  @DataProvider(name = "speechless")
  public static Object[][] speechless() {
    return new Object[][] {{new Speechless()}};
  }

  @Test(dataProvider = "speechless")
  public void report(Speechless nothingToSay) {}

  /** Renders as nothing at all, which every report has to decide what to write for. */
  public static final class Speechless {

    @Override
    public @Nullable String toString() {
      return null;
    }
  }
}
