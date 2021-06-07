package org.testng.internal.invokers;

import java.util.Set;
import org.testng.IInvokedMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

/** A utility class that houses mechanisms to retrieve exception information. */
final class ExceptionUtils {
  private ExceptionUtils() {
    // Utility class. Defeat instantiation.
  }

  static Throwable getExceptionDetails(ITestContext context, Object instance) {
    Set<ITestResult> configResults = context.getFailedConfigurations().getAllResults();
    if (configResults.isEmpty()) {
      configResults = context.getSkippedConfigurations().getAllResults();
    }
    if (configResults.isEmpty()) {
      // if we are here it means we have a test method skip due to a @BeforeSuite failure in a
      // different <test> maybe
      // So we will have to find out that first failure/skip and get its throwable and pack that
      // information into our
      // current test method's test result.
      return getConfigFailureException(context);
    }
    for (ITestResult configResult : configResults) {
      if (sameInstance(configResult, instance)) {
        return configResult.getThrowable();
      }
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

  private static Throwable getConfigFailureException(ITestContext context) {
    for (IInvokedMethod method : context.getSuite().getAllInvokedMethods()) {
      ITestNGMethod m = method.getTestMethod();
      if (m.isBeforeSuiteConfiguration() && (!method.getTestResult().isSuccess())) {
        return method.getTestResult().getThrowable();
      }
    }
    return null;
  }
}
