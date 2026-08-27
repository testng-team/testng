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
  private static final int WIDTH = 60;

  public static String first() {
    return repeat('a', WIDTH);
  }

  public static String second() {
    return repeat('b', WIDTH);
  }

  private static String repeat(char letter, int times) {
    StringBuilder value = new StringBuilder();
    for (int i = 0; i < times; i++) {
      value.append(letter);
    }
    return value.toString();
  }

  @DataProvider(name = "widths")
  public static Object[][] widths() {
    // 60 + ", " + 60 = 122 characters joined, and 5 + ", " + 5 = 12.
    return new Object[][] {{first(), second()}, {"short", "brief"}};
  }

  @Test(dataProvider = "widths")
  public void report(String left, String right) {}
}
