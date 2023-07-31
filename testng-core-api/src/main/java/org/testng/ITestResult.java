package org.testng;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.testng.internal.thread.ThreadTimeoutException;

/**
 * This class describes the result of a test.
 *
 * @author Cedric Beust, May 2, 2004
 * @version $Revision: 721 $, $Date: 2009-05-23 09:55:46 -0700 (Sat, 23 May 2009) $
 * @since May 2, 2004
 */
public interface ITestResult extends IAttributes, Comparable<ITestResult> {

  // Test status
  int CREATED = -1;
  int SUCCESS = 1;
  int FAILURE = 2;
  int SKIP = 3;
  int SUCCESS_PERCENTAGE_FAILURE = 4;
  int STARTED = 16;

  /** Returns the status of this result, using one of the constants above. */
  int getStatus();

  void setStatus(int status);

  /** Returns the test method this result represents. */
  ITestNGMethod getMethod();

  /** Returns the parameters this method was invoked with. */
  Object[] getParameters();

  void setParameters(Object[] parameters);

  /** Returns the test class used this object is a result for. */
  IClass getTestClass();

  /**
   * Returns the throwable that was thrown while running the method, or null if no exception was
   * thrown.
   */
  Throwable getThrowable();

  void setThrowable(Throwable throwable);

  /** Returns the start date for this test, in milliseconds. */
  long getStartMillis();

  /** Returns the end date for this test, in milliseconds. */
  long getEndMillis();

  void setEndMillis(long millis);

  /** Returns the name of this TestResult, typically identical to the name of the method. */
  String getName();

  /** Returns true if if this test run is a SUCCESS */
  boolean isSuccess();

  /**
   * Returns the host where this suite was run, or null if it was run locally. The returned string
   * has the form: host:port
   */
  String getHost();

  /** Returns the instance on which this method was run. */
  Object getInstance();

  /**
   * Returns a parameter array that was passed to a factory method (or) an empty object array
   * otherwise.
   */
  Object[] getFactoryParameters();

  /**
   * Returns the test name if this result's related instance implements ITest or
   * use @Test(testName=...), null otherwise.
   */
  String getTestName();

  String getInstanceName();

  /** Returns the {@link ITestContext} for this test result. */
  ITestContext getTestContext();

  /** @param name - The new name to be used as a test name */
  void setTestName(String name);

  /**
   * Returns <code>true</code> if the test was retried again by an implementation of {@link
   * IRetryAnalyzer}.
   */
  boolean wasRetried();

  /**
   * @param wasRetried - <code>true</code> if the test was retried and <code>false</code> otherwise.
   */
  void setWasRetried(boolean wasRetried);

  /**
   * Returns the list of either upstream method(s) or configuration method(s) whose failure led to
   * the current method being skipped. An empty list is returned when the current method is not a
   * skipped method.
   */
  default List<ITestNGMethod> getSkipCausedBy() {
    return Collections.emptyList();
  }

  /**
   * Returns a unique id for the current JVM that represents a unique way of identifying a specific
   * test method's result.
   */
  String id();

  /**
   * Returns <code>true</code> if the current test result is either {@link ITestResult#STARTED} or
   * {@link ITestResult#CREATED}
   */
  default boolean isNotRunning() {
    return getStatus() == STARTED || getStatus() == CREATED;
  }

  /**
   * Returns a list of all user facing statuses viz.,
   *
   * <ul>
   *   <li>{@link ITestResult#SUCCESS}
   *   <li>{@link ITestResult#SUCCESS_PERCENTAGE_FAILURE}
   *   <li>{@link ITestResult#FAILURE}
   *   <li>{@link ITestResult#SKIP}
   * </ul>
   */
  static List<String> finalStatuses() {
    return Arrays.asList("SUCCESS", "FAILURE", "SKIP", "SUCCESS_PERCENTAGE_FAILURE");
  }

  /**
   * Returns <code>true</code> if the test failure was due to a timeout.
   *
   * @param result - The test result of a method
   */
  static boolean wasFailureDueToTimeout(ITestResult result) {
    Throwable cause = result.getThrowable();
    while (cause != null && !cause.getClass().equals(Throwable.class)) {
      if (cause instanceof ThreadTimeoutException) {
        return true;
      }
      cause = cause.getCause();
    }
    return false;
  }
}
