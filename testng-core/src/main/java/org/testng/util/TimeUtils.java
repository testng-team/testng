package org.testng.util;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.TimeZone;
import org.testng.internal.RuntimeBehavior;
import org.testng.internal.Utils;

/** A Utility class that deals with time. */
public final class TimeUtils {
  private TimeUtils() {}

  /**
   * @param timeInMilliSeconds - The time in milliseconds
   * @param format - A format that can be used by {@link SimpleDateFormat}
   * @return - A formatted string representation of the time in the timezone as obtained via {@link
   *     RuntimeBehavior#getTimeZone()}
   */
  public static String formatTimeInLocalOrSpecifiedTimeZone(
      long timeInMilliSeconds, String format) {
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    TimeZone timeZone = RuntimeBehavior.getTimeZone();
    sdf.setTimeZone(timeZone);
    return sdf.format(timeInMilliSeconds);
  }

  /** A sample task to be executed. */
  @FunctionalInterface
  public interface Task {

    /** The actual work to be executed. */
    void execute();
  }

  /**
   * Helper method that can be used to compute the time.
   *
   * @param msg - A user friendly message to be shown in the logs.
   * @param task - A {@link Task} that represents the task to be executed.
   */
  public static void computeAndShowTime(String msg, Task task) {
    Instant start = Instant.now();
    try {
      task.execute();
    } finally {
      Instant finish = Instant.now();
      long timeElapsed = Duration.between(start, finish).toMillis();
      String text = msg + " took " + timeElapsed + " ms.";
      Utils.log(text);
      if (timeElapsed > 20000) {
        Utils.log("[WARNING] Probable slow call ( > 20 seconds): " + text);
      }
    }
  }
}
