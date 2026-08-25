package org.testng.reporters.snapshot;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * {@link RenderingCountSample} with a configuration method that passes, which is how a test tells
 * one reporting representation of a passing configuration from two: a snapshot dropped before the
 * reports run is a snapshot the fallback has to render a second time.
 *
 * <p>Its own counter rather than the one next door, since adding a configuration method to {@link
 * RenderingCountSample} would move the counts the tests of that sample assert on.
 */
public class CountedConfigurationParameterSample {

  private static final AtomicInteger RENDERINGS = new AtomicInteger();

  /**
   * The count is of the class, as {@link RenderingCountSample#renderings()} explains: a caller
   * reads it either side of the run it is measuring and takes the difference.
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

  /** Handed the row its test method will run with, and passing -- it changes nothing. */
  @BeforeMethod
  public void prepare(Object[] parameters) {}

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
