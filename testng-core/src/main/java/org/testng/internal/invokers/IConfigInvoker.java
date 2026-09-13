package org.testng.internal.invokers;

import org.jspecify.annotations.Nullable;
import org.testng.IClass;
import org.testng.ITestNGMethod;
import org.testng.internal.IConfiguration;

public interface IConfigInvoker {

  /** The mark that ignores no recorded failure: every failure counts. */
  long NO_IGNORED_FAILURES = 0L;

  boolean hasConfigurationFailureFor(
      @Nullable ITestNGMethod testNGMethod,
      String[] groups,
      IClass testClass,
      @Nullable Object instance);

  /**
   * Answers whether a recorded configuration failure applies to a method, ignoring the failures
   * recorded up to a mark.
   *
   * @param configMethod the configuration method being scrutinised, or null to ask about the class
   *     as a whole
   * @param testNGMethod null when the configuration is a class or suite level one, which has no
   *     current test method
   * @param ignoredFailureMark a value of {@link #currentFailureMark()} taken earlier; the failures
   *     recorded up to that point do not count. {@link #NO_IGNORED_FAILURES} counts every failure.
   */
  boolean hasConfigurationFailureFor(
      @Nullable ITestNGMethod configMethod,
      @Nullable ITestNGMethod testNGMethod,
      String[] groups,
      IClass testClass,
      @Nullable Object instance,
      long ignoredFailureMark);

  /**
   * Answers where the record of configuration failures currently stands. An attempt that retries a
   * failed test method takes this mark before it starts and hands it back through {@code
   * ignoredFailureMark}, so that what the failed attempt recorded does not hold the retry back
   * while a failure during the retry still does.
   *
   * @return a mark to pass as {@code ignoredFailureMark}
   */
  long currentFailureMark();

  void invokeBeforeGroupsConfigurations(GroupConfigMethodArguments arguments);

  void invokeAfterGroupsConfigurations(GroupConfigMethodArguments arguments);

  void invokeConfigurations(ConfigMethodArguments arguments);

  IConfiguration getConfiguration();
}
