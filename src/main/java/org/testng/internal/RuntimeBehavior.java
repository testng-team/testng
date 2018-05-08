package org.testng.internal;

import java.util.TimeZone;

/**
 * This class houses handling all JVM arguments by TestNG
 */
public final class RuntimeBehavior {

    public static final String TESTNG_LISTENERS_ALWAYSRUN = "testng.listeners.alwaysrun";

    private RuntimeBehavior() {
    }

    /**
     * @return - returns <code>true</code> if we would like to run in the Dry mode and
     * <code>false</code> otherwise.
     */
    public static boolean isDryRun() {
        String value = System.getProperty("testng.mode.dryrun", "false");
        return Boolean.parseBoolean(value);
    }

    /**
     * @return - returns the {@link TimeZone} corresponding to the JVM argument
     * <code>-Dtestng.timezone</code> if it was set. If not set, it returns the default
     * timezone pertaining to the user property <code>user.timezone</code>
     */
    public static TimeZone getTimeZone() {
        String timeZone = System.getProperty("testng.timezone", "");
        if (timeZone.trim().isEmpty()) {
            return TimeZone.getDefault();
        }
        return TimeZone.getTimeZone(timeZone);
    }

  /**
   * @return - <code>true</code> if we would like to invoke applicable listeners for tests that
   *     would be skipped by TestNG due to a configuration failure (or) due to a upstream dependency
   *     failure.
   */
  public static boolean invokeListenersForSkippedTests() {
    return Boolean.parseBoolean(System.getProperty(TESTNG_LISTENERS_ALWAYSRUN, "false"));
  }
}
