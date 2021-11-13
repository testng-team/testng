package org.testng.internal.invokers;

import static org.testng.internal.Parameters.MethodParameters;

import java.util.Map;
import org.testng.DataProviderHolder;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.ITestResult;
import org.testng.internal.Parameters;
import org.testng.internal.TestResult;
import org.testng.internal.Utils;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.util.Strings;
import org.testng.xml.XmlSuite;

class ParameterHandler {
  private final ITestObjectFactory objectFactory;
  private final IAnnotationFinder finder;
  private final DataProviderHolder holder;
  private int verbose;

  ParameterHandler(
      ITestObjectFactory objectFactory,
      IAnnotationFinder finder,
      DataProviderHolder holder,
      int verbose) {
    this.objectFactory = objectFactory;
    this.finder = finder;
    this.holder = holder;
    this.verbose = verbose;
  }

  ParameterBag createParameters(
      ITestNGMethod testMethod,
      Map<String, String> parameters,
      Map<String, String> allParameterNames,
      ITestContext testContext) {
    return createParameters(testMethod, parameters, allParameterNames, testContext, null);
  }

  ParameterBag createParameters(
      ITestNGMethod testMethod,
      Map<String, String> parameters,
      Map<String, String> allParameterNames,
      ITestContext testContext,
      Object fedInstance) {
    return handleParameters(
        testMethod,
        testMethod.getInstance(),
        allParameterNames,
        parameters,
        testContext,
        fedInstance);
  }

  private ParameterBag handleParameters(
      ITestNGMethod testMethod,
      Object instance,
      Map<String, String> allParameterNames,
      Map<String, String> parameters,
      ITestContext testContext,
      Object fedInstance) {
    XmlSuite suite = testContext.getCurrentXmlTest().getSuite();
    try {
      MethodParameters methodParams =
          MethodParameters.newInstance(parameters, testMethod, testContext);
      ParameterHolder paramHolder =
          Parameters.handleParameters(
              objectFactory,
              testMethod,
              allParameterNames,
              instance,
              methodParams,
              suite,
              finder,
              fedInstance,
              holder);
      return new ParameterBag(paramHolder);
    } catch (Throwable cause) {
      if (verbose >= 2) {
        String msg =
            Utils.longStackTrace(cause.getCause() != null ? cause.getCause() : cause, true);
        if (Strings.isNotNullAndNotEmpty(msg)) {
          Utils.error(msg);
        }
      }

      ITestResult result = TestResult.newTestResultWithCauseAs(testMethod, testContext, cause);
      return new ParameterBag(result);
    }
  }

  /**
   * This class holds a {@code ParameterHolder} or in case of an error, a non-null {@code
   * TestResult} containing the cause
   */
  static class ParameterBag {
    final ParameterHolder parameterHolder;
    final ITestResult errorResult;

    ParameterBag(ParameterHolder parameterHolder) {
      this.parameterHolder = parameterHolder;
      this.errorResult = null;
    }

    ParameterBag(ITestResult errorResult) {
      this.parameterHolder = null;
      this.errorResult = errorResult;
    }

    boolean hasErrors() {
      return errorResult != null;
    }

    boolean runInParallel() {
      return ((parameterHolder != null)
          && (parameterHolder.origin == ParameterHolder.ParameterOrigin.ORIGIN_DATA_PROVIDER
              && parameterHolder.dataProviderHolder.isParallel()));
    }
  }
}
