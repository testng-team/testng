package org.testng.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * A Utility class that deals with time.
 */
public final class TimeUtils {
    private TimeUtils() {
    }

    /**
     * @param timeInMilliSeconds - The time in milliseconds
     * @param format             - A format that can be used by {@link SimpleDateFormat}
     * @return - A formatted string representation of the time in UTC/GMT timezone.
     */
    public static String timeInUTC(long timeInMilliSeconds, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        TimeZone utc = TimeZone.getTimeZone("UTC");
        sdf.setTimeZone(utc);
        return sdf.format(timeInMilliSeconds);
    }
}
