package org.testng.internal.invokers;

import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.testng.IInvokedMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

/** A utility class that houses mechanisms to retrieve exception information. */
final class ExceptionUtils {
  private ExceptionUtils() {
    // Utility class. Defeat instantiation.
  }

  static @Nullable Throwable getExceptionDetails(ITestContext context, Object instance) {
    Set<ITestResult> failedConfigs = context.getFailedConfigurations().getAllResults();
    Set<ITestResult> skippedConfigs = context.getSkippedConfigurations().getAllResults();
    // The configuration of the test's own instance comes first, whether it failed or was skipped.
    // A failure in another class must not hide a SkipException thrown by this class's setup.
    for (Set<ITestResult> results : List.of(failedConfigs, skippedConfigs)) {
      for (ITestResult configResult : results) {
        if (sameInstance(configResult, instance) && configResult.getThrowable() != null) {
          return configResult.getThrowable();
        }
      }
    }
    Set<ITestResult> configResults = failedConfigs.isEmpty() ? skippedConfigs : failedConfigs;
    if (configResults.isEmpty()) {
      // if we are here it means we have a test method skip due to a @BeforeSuite failure in a
      // different <test> maybe
      // So we will have to find out that first failure/skip and get its throwable and pack that
      // information into our
      // current test method's test result.
      return getConfigFailureException(context);
    }
    // If we are here it perhaps means that the test method is being skipped because there was a
    // configuration
    // failure in a different class due to @BeforeGroups being used.
    // So lets just find the first exception information and then just pack it in.
    return configResults.iterator().next().getThrowable();
  }

  private static boolean sameInstance(ITestResult configResult, Object instance) {
    return instance.equals(configResult.getInstance());
  }

  private static @Nullable Throwable getConfigFailureException(ITestContext context) {
    for (IInvokedMethod method : context.getSuite().getAllInvokedMethods()) {
      ITestNGMethod m = method.getTestMethod();
      if (m.isBeforeSuiteConfiguration() && !method.getTestResult().isSuccess()) {
        return method.getTestResult().getThrowable();
      }
    }
    return null;
  }
}
