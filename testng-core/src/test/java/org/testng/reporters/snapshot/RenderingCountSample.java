package org.testng.reporters.snapshot;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A parameter that counts the times it was rendered, which is how a test tells one reporting
 * representation of an invocation from two.
 */
public class RenderingCountSample {

  private static final AtomicInteger RENDERINGS = new AtomicInteger();

  /**
   * The count is of the class, since nothing outside a run holds the parameter a data provider
   * made. A caller reads it either side of the run it is measuring and takes the difference, rather
   * than zeroing it and depending on being the only run.
   *
   * @return - How many times a parameter of this sample has been rendered, ever.
   */
  public static int renderings() {
    return RENDERINGS.get();
  }

  @DataProvider(name = "counted")
  public static Object[][] counted() {
    return new Object[][] {{new CountedParameter()}};
  }

  @Test(dataProvider = "counted")
  public void report(CountedParameter parameter) {}

  /** Reports being rendered, and is otherwise the plainest parameter there is. */
  public static final class CountedParameter {

    @Override
    public String toString() {
      RENDERINGS.incrementAndGet();
      return "counted";
    }
  }
}
