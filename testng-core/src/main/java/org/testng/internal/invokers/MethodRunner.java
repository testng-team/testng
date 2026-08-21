package org.testng.internal.invokers;

import static java.util.concurrent.CompletableFuture.allOf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.collections.CollectionUtils;
import org.testng.internal.IConfiguration;
import org.testng.internal.ObjectBag;
import org.testng.internal.Parameters;
import org.testng.internal.invokers.ITestInvoker.FailureContext;
import org.testng.internal.invokers.TestMethodArguments.Builder;
import org.testng.internal.thread.Async;
import org.testng.internal.thread.TestNGThreadFactory;
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
    List<ITestResult> result = new ArrayList<>();
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
              next, arguments.getTestMethod().getConstructorOrMethod().requireMethod(), context);

      List<ITestResult> tmpResults = new ArrayList<>();
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
          lastSuccess = tmpResults.get(tmpResultsIndex).getStatus() == ITestResult.SUCCESS;
        }
        if (failure.instances.isEmpty() || lastSuccess) {
          result.addAll(tmpResults);
        } else {
          List<ITestResult> retryResults = new ArrayList<>();
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
            result.add(
                testInvoker.registerCancelledInvocation(
                    tmArguments.getTestMethod(), System.currentTimeMillis(), parameterValues));
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
    ObjectBag objectBag = ObjectBag.getInstance(context.getSuite());
    boolean reUse = suite.isShareThreadPoolForDataProviders() || suite.useGlobalThreadPool();

    ExecutorService service = getOrCreate(reUse, suite, objectBag, testInvoker.getConfiguration());

    List<TestMethodWithDataProviderMethodWorker> workers = new ArrayList<>();
    int parametersIndex = 0;
    for (Object[] next : CollectionUtils.asIterable(allParamValues)) {
      if (next == null) {
        // skipped value
        parametersIndex += 1;
        continue;
      }
      Object[] parameterValues =
          Parameters.injectParameters(
              next, arguments.getTestMethod().getConstructorOrMethod().requireMethod(), context);

      workers.add(
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
              failure.count.get()));
      // testng387: increment the param index in the bag.
      parametersIndex += 1;
    }

    try {
      if (service instanceof ForkJoinPool) {
        return runWithWorkStealing((ForkJoinPool) service, workers);
      }
      return runAsync(service, workers);
    } finally {
      if (!reUse) {
        service.shutdown();
      }
    }
  }

  /**
   * Runs the data-rows on the shared, global {@link ForkJoinPool}. The data-driven test method that
   * triggers this executes on a worker of that very same pool, so joining the per-row {@link
   * ForkJoinTask}s makes the calling worker actively help run the still-pending rows
   * (work-stealing) instead of parking a pooled thread while it waits. That keeps the pool busy -
   * rather than only {@code (thread-count - number-of-data-driven-tests)} threads - so the
   * effective parallelism no longer degrades as more data-driven tests are added, and the pool can
   * never be dead-locked by parked workers. See GITHUB-3242.
   */
  private static List<ITestResult> runWithWorkStealing(
      ForkJoinPool pool, List<TestMethodWithDataProviderMethodWorker> workers) {
    List<ForkJoinTask<List<ITestResult>>> tasks = new ArrayList<>(workers.size());
    for (TestMethodWithDataProviderMethodWorker worker : workers) {
      tasks.add(pool.submit(worker));
    }
    // First, wait for the whole batch to finish. quietlyJoin() blocks (helping via work-stealing)
    // but never throws, so one failing row cannot stop us waiting for the rest - once this loop
    // ends every row has completed and none is left running. This mirrors the async path's all-of
    // semantics. See GITHUB-3242.
    for (ForkJoinTask<List<ITestResult>> task : tasks) {
      task.quietlyJoin();
    }
    // Now that every row has finished, collect the results. join() here only reports an
    // already-computed outcome: it returns the result, or re-throws the first failure (exactly as
    // the async path did) without leaving any sibling row still running.
    List<ITestResult> results = new ArrayList<>();
    for (ForkJoinTask<List<ITestResult>> task : tasks) {
      results.addAll(task.join());
    }
    return results;
  }

  /**
   * Runs the data-rows on a regular (non {@link ForkJoinPool}) {@link ExecutorService}, i.e. a
   * dedicated data-provider thread-pool that is distinct from the one running the test methods.
   */
  private static List<ITestResult> runAsync(
      ExecutorService service, List<TestMethodWithDataProviderMethodWorker> workers) {
    List<CompletableFuture<List<ITestResult>>> all = new ArrayList<>(workers.size());
    for (TestMethodWithDataProviderMethodWorker worker : workers) {
      all.add(Async.run(worker, service));
    }

    // Wait for the data-rows to finish and then collect their results, preserving submission order.
    CompletableFuture<Void> combined = allOf(all.toArray(new CompletableFuture[0]));
    return combined
        .thenApply(
            ignored ->
                all.stream()
                    .map(CompletableFuture::join)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()))
        .join();
  }

  private static ExecutorService getOrCreate(
      boolean reUse, XmlSuite suite, ObjectBag objectBag, IConfiguration configuration) {
    if (reUse && suite.useGlobalThreadPool()) {
      // Reuse the single common pool that is shared with the regular test-method execution
      // (normally already created by TestTaskExecutor via the same factory method). Running the
      // data-rows on it lets the data-driven method worker help execute them via work-stealing
      // (see runWithWorkStealing) rather than parking and starving the pool. See GITHUB-3242.
      return (ExecutorService)
          objectBag.createIfRequired(
              ExecutorService.class,
              () ->
                  configuration
                      .getExecutorServiceFactory()
                      .createGlobalThreadPool(
                          suite.getThreadCount(), ThreadUtil.THREAD_NAME + "-test"));
    }
    // Dedicated data-provider pool (shared across the suite when isShareThreadPoolForDataProviders
    // is set, otherwise per data-driven method). Built through the configured factory so a custom
    // -threadpoolfactoryclass is honoured here too, matching TestTaskExecutor and ThreadUtil.
    int dataProviderThreadCount = suite.getDataProviderThreadCount();
    Supplier<Object> supplier =
        () ->
            configuration
                .getExecutorServiceFactory()
                .create(
                    dataProviderThreadCount,
                    dataProviderThreadCount,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(),
                    threadFactory());
    if (reUse) {
      return (ExecutorService) objectBag.createIfRequired(ExecutorService.class, supplier);
    }
    return (ExecutorService) supplier.get();
  }

  private static ThreadFactory threadFactory() {
    return new TestNGThreadFactory("PoolService");
  }
}
