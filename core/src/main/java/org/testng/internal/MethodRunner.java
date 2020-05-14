package org.testng.internal;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.collections.CollectionUtils;
import org.testng.collections.Lists;
import org.testng.internal.ITestInvoker.FailureContext;
import org.testng.internal.TestMethodArguments.Builder;
import org.testng.xml.XmlSuite;

class MethodRunner implements IMethodRunner {

  @Override
  public List<ITestResult> runInSequence(TestMethodArguments arguments,
      ITestInvoker testInvoker,
      ITestContext context,
      AtomicInteger invocationCount,
      FailureContext failure,
      Iterator<Object[]> allParamValues,
      boolean skipFailedInvocationCounts) {
    List<ITestResult> result = Lists.newArrayList();
    int parametersIndex = 0;
    Iterable<Object[]> allParameterValues = CollectionUtils.asIterable(allParamValues);
    for (Object[] next: allParameterValues) {
      if (next == null) {
        // skipped value
        parametersIndex++;
        continue;
      }
      Object[] parameterValues =
          Parameters.injectParameters(
              next, arguments.getTestMethod().getConstructorOrMethod().getMethod(), context);

      List<ITestResult> tmpResults = Lists.newArrayList();
      int tmpResultsIndex = -1;
      TestMethodArguments tmArguments = new Builder()
          .usingArguments(arguments)
          .withParameterValues(parameterValues)
          .withParametersIndex(parametersIndex)
          .build();
      try {
        ITestResult tmpResult = testInvoker
            .invokeTestMethod(tmArguments, context.getSuite().getXmlSuite(), failure);
        tmpResults.add(tmpResult);
        tmpResultsIndex++;
      } finally {
        boolean lastSucces = false;
        if (tmpResultsIndex >= 0) {
          lastSucces = (tmpResults.get(tmpResultsIndex).getStatus() == ITestResult.SUCCESS);
        }
        if (failure.instances.isEmpty() || lastSucces) {
          result.addAll(tmpResults);
        } else {
          List<ITestResult> retryResults = Lists.newArrayList();
          failure = testInvoker.retryFailed(tmArguments, retryResults, failure.count, context);
          result.addAll(retryResults);
        }

        // If we have a failure, skip all the
        // other invocationCounts
        if (failure.count > 0
            && (skipFailedInvocationCounts || tmArguments.getTestMethod()
            .skipFailedInvocations())) {
          while (invocationCount.getAndDecrement() > 0) {
            ITestResult r = testInvoker
                .registerSkippedTestResult(tmArguments.getTestMethod(), System.currentTimeMillis(),
                    null);
            result.add(r);
            InvokedMethod invokedMethod =
                new InvokedMethod(r.getInstance(), tmArguments.getTestMethod(),
                    System.currentTimeMillis(), r);
            testInvoker.invokeListenersForSkippedTestResult(r, invokedMethod);
          }
        }
      } // end finally
      parametersIndex++;
    }
    return result;
  }

  @Override
  public List<ITestResult> runInParallel(TestMethodArguments arguments,
      ITestInvoker testInvoker,
      ITestContext context,
      AtomicInteger invocationCount,
      FailureContext failure,
      Iterator<Object[]> allParamValues,
      boolean skipFailedInvocationCounts) {
    XmlSuite suite = context.getSuite().getXmlSuite();
    List<ITestResult> result = Lists.newArrayList();
    List<TestMethodWithDataProviderMethodWorker> workers = Lists.newArrayList();
    int parametersIndex = 0;
    Iterable<Object[]> allParameterValues = CollectionUtils.asIterable(allParamValues);
    for (Object[] next : allParameterValues) {
      if (next == null) {
        // skipped value
        parametersIndex += 1;
        continue;
      }
      Object[] parameterValues =
          Parameters.injectParameters(
              next, arguments.getTestMethod().getConstructorOrMethod().getMethod(), context);

      TestMethodWithDataProviderMethodWorker w =
          new TestMethodWithDataProviderMethodWorker(
              testInvoker, arguments.getTestMethod(),
              parametersIndex,
              parameterValues,
              arguments.getInstance(),
              arguments.getParameters(),
              arguments.getTestClass(),
              arguments.getBeforeMethods(),
              arguments.getAfterMethods(),
              arguments.getGroupMethods(),
              context,
              skipFailedInvocationCounts,
              invocationCount.get(),
              failure.count,
              testInvoker.getNotifier());
      workers.add(w);
      // testng387: increment the param index in the bag.
      parametersIndex += 1;
    }
    PoolService<List<ITestResult>> ps = new PoolService<>(suite.getDataProviderThreadCount());
    List<List<ITestResult>> r = ps.submitTasksAndWait(workers);
    for (List<ITestResult> l2 : r) {
      result.addAll(l2);
    }
    return result;
  }
}
