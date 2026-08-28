package org.testng.reporters.snapshot;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Two invocations either side of the hundred character limit the jq report truncates a method name
 * at: one whose joined values are longer, one whose are shorter.
 *
 * <p>The values are runs of a single letter so that where the truncation falls is legible in the
 * expectation rather than having to be counted.
 */
public class LongParameterSample {

  /** Long enough that the joined form is over the limit, but not by so much it is unreadable. */
  public static final String FIRST = "a".repeat(60);

  public static final String SECOND = "b".repeat(60);

  @DataProvider(name = "widths")
  public static Object[][] widths() {
    return new Object[][] {{FIRST, SECOND}, {"short", "brief"}};
  }

  @Test(dataProvider = "widths")
  public void report(String left, String right) {}
}
