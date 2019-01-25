package org.testng.internal;

import java.util.TimeZone;

/** This class houses handling all JVM arguments by TestNG */
public final class RuntimeBehavior {

  public static final String TESTNG_THREAD_AFFINITY = "testng.thread.affinity";
  public static final String TESTNG_MODE_DRYRUN = "testng.mode.dryrun";
  private static final String TEST_CLASSPATH = "testng.test.classpath";
  private static final String SKIP_CALLER_CLS_LOADER = "skip.caller.clsLoader";
  public static final String SHOW_TESTNG_STACK_FRAMES = "testng.show.stack.frames";

  private RuntimeBehavior() {}

  public static boolean showTestNGStackFrames() {
    return Boolean.getBoolean(SHOW_TESTNG_STACK_FRAMES);
  }

  public static String getDefaultLineSeparator() {
    return System.getProperty("line.separator");
  }

  public static String getCurrentUserHome() {
    return System.getProperty("user.home");
  }

  public static String getDefaultDataProviderThreadCount() {
    return System.getProperty("dataproviderthreadcount", "");
  }

  public static String getDefaultXmlGenerationImpl() {
    return System.getProperty("testng.xml.weaver", "org.testng.xml.DefaultXmlWeaver");
  }

  public static boolean isTestMode() {
    return Boolean.parseBoolean(System.getProperty("testng.testmode"));
  }

  public static boolean shouldSkipUsingCallerClassLoader() {
    return Boolean.parseBoolean(System.getProperty(SKIP_CALLER_CLS_LOADER));
  }

  public static boolean useStrictParameterMatching() {
    return Boolean.parseBoolean(System.getProperty("strictParameterMatch"));
  }

  public static String orderMethodsBasedOn() {
    return System.getProperty("testng.order", Systematiser.Order.INSTANCES.getValue());
  }

  public static String getTestClasspath() {
    return System.getProperty(TEST_CLASSPATH);
  }

  public static boolean useOldTestNGEmailableReporter() {
    return System.getProperty("oldTestngEmailableReporter") != null;
  }

  public static boolean useEmailableReporter() {
    return System.getProperty("noEmailableReporter") == null;
  }

  /**
   * @return - returns <code>true</code> if we would like to run in the Dry mode and <code>false
   *     </code> otherwise.
   */
  public static boolean isDryRun() {
    String value = System.getProperty(TESTNG_MODE_DRYRUN, "false");
    return Boolean.parseBoolean(value);
  }

  /**
   * @return - returns the {@link TimeZone} corresponding to the JVM argument <code>
   *     -Dtestng.timezone</code> if it was set. If not set, it returns the default timezone
   *     pertaining to the user property <code>user.timezone</code>
   */
  public static TimeZone getTimeZone() {
    String timeZone = System.getProperty("testng.timezone", "");
    if (timeZone.trim().isEmpty()) {
      return TimeZone.getDefault();
    }
    return TimeZone.getTimeZone(timeZone);
  }

  /**
   * @return - <code>true</code> if we would like to enforce Thread affinity when dealing with the
   *     below two variants of execution models:
   *     <ul>
   *       <li>Ordering priority
   *       <li>Ordering by dependsOnMethods (will not work with dependency on multiple methods)
   *     </ul>
   */
  public static boolean enforceThreadAffinity() {
    return Boolean.parseBoolean(System.getProperty(TESTNG_THREAD_AFFINITY, "false"));
  }
}
