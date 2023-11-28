package org.testng.internal.invokers;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.collections.CollectionUtils;
import org.testng.collections.Lists;
import org.testng.internal.ObjectBag;
import org.testng.internal.Parameters;
import org.testng.internal.invokers.ITestInvoker.FailureContext;
import org.testng.internal.invokers.TestMethodArguments.Builder;
import org.testng.internal.thread.ThreadUtil;
import org.testng.xml.XmlSuite;

public class MethodRunner implements IMethodRunner {

  @Override
  public List<ITestResult> runInSequence(
      TestMethodArguments arguments,
      ITestInvoker testInvoker,
      ITestContext context,
      AtomicInteger invocationCount,
      FailureContext failure,
      Iterator<Object[]> allParamValues,
      boolean skipFailedInvocationCounts) {
    List<ITestResult> result = Lists.newArrayList();
    int parametersIndex = 0;
    Iterable<Object[]> allParameterValues = CollectionUtils.asIterable(allParamValues);
    for (Object[] next : allParameterValues) {
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
      TestMethodArguments tmArguments =
          new Builder()
              .usingArguments(arguments)
              .withParameterValues(parameterValues)
              .withParametersIndex(parametersIndex)
              .build();
      try {
        ITestResult tmpResult =
            testInvoker.invokeTestMethod(tmArguments, context.getSuite().getXmlSuite(), failure);
        tmpResults.add(tmpResult);
        tmpResultsIndex++;
      } finally {
        boolean lastSuccess = false;
        if (tmpResultsIndex >= 0) {
          lastSuccess = (tmpResults.get(tmpResultsIndex).getStatus() == ITestResult.SUCCESS);
        }
        if (failure.instances.isEmpty() || lastSuccess) {
          result.addAll(tmpResults);
        } else {
          List<ITestResult> retryResults = Lists.newArrayList();
          failure =
              testInvoker.retryFailed(tmArguments, retryResults, failure.count.get(), context);
          result.addAll(retryResults);
        }

        // If we have a failure, skip all the
        // other invocationCounts
        if (failure.count.get() > 0
            && (skipFailedInvocationCounts
                || tmArguments.getTestMethod().skipFailedInvocations())) {
          while (invocationCount.getAndDecrement() > 0) {
            ITestResult r =
                testInvoker.registerSkippedTestResult(
                    tmArguments.getTestMethod(), System.currentTimeMillis(), null);
            result.add(r);
            InvokedMethod invokedMethod = new InvokedMethod(System.currentTimeMillis(), r);
            testInvoker.invokeListenersForSkippedTestResult(r, invokedMethod);
          }
        }
      } // end finally
      parametersIndex++;
    }
    return result;
  }

  @Override
  public List<ITestResult> runInParallel(
      TestMethodArguments arguments,
      ITestInvoker testInvoker,
      ITestContext context,
      AtomicInteger invocationCount,
      FailureContext failure,
      Iterator<Object[]> allParamValues,
      boolean skipFailedInvocationCounts) {
    XmlSuite suite = context.getSuite().getXmlSuite();
    int parametersIndex = 0;
    ObjectBag objectBag = ObjectBag.getInstance(context.getSuite());
    boolean reUse = suite.isShareThreadPoolForDataProviders();

    ExecutorService service = getOrCreate(reUse, suite.getDataProviderThreadCount(), objectBag);
    List<CompletableFuture<List<ITestResult>>> all = new ArrayList<>();
    for (Object[] next : CollectionUtils.asIterable(allParamValues)) {
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
              testInvoker,
              arguments.getTestMethod(),
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
              failure.count.get(),
              testInvoker.getNotifier());
      all.add(supplyAsync(w::call, service));
      // testng387: increment the param index in the bag.
      parametersIndex += 1;
    }

    // don't block on execution of any of the completablefuture
    CompletableFuture<Void> combined = allOf(all.toArray(new CompletableFuture[0]));

    // Now start processing the results of each of the CompletableFutures as and when they
    // become available
    List<ITestResult> result =
        combined
            .thenApply(
                ignored ->
                    all.stream()
                        .map(CompletableFuture::join)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()))
            .join();
    if (!reUse) {
      service.shutdown();
    }
    return result;
  }

  private static ExecutorService getOrCreate(boolean reUse, int count, ObjectBag objectBag) {
    if (reUse) {
      return (ExecutorService)
          objectBag.createIfRequired(
              ExecutorService.class, () -> Executors.newFixedThreadPool(count, threadFactory()));
    }
    return Executors.newFixedThreadPool(count, threadFactory());
  }

  private static ThreadFactory threadFactory() {
    AtomicInteger threadNumber = new AtomicInteger(0);
    return r ->
        new Thread(r, ThreadUtil.THREAD_NAME + "-PoolService-" + threadNumber.getAndIncrement());
  }
}
