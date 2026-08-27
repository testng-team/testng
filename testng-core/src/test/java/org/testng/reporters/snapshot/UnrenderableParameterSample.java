package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A parameter that cannot be rendered at all: its {@code toString()} throws.
 *
 * <p>Rendering a value runs the user's code, and a report has nowhere to fail from -- it describes
 * an invocation that is already over. GITHUB-2830 is what this costs when it is not guarded.
 */
public class UnrenderableParameterSample {

  @DataProvider(name = "unrenderable")
  public static Object[][] unrenderable() {
    return new Object[][] {{new Unrenderable()}};
  }

  @Test(dataProvider = "unrenderable")
  public void report(Unrenderable parameter) {}

  /** A value that cannot be asked what it is, as opposed to one that answers {@code null}. */
  public static final class Unrenderable {

    @Override
    public String toString() {
      throw new IllegalStateException("this parameter cannot be rendered");
    }
  }
}
