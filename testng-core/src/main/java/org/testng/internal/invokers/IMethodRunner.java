package org.testng.internal.invokers;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.invokers.ITestInvoker.FailureContext;

public interface IMethodRunner {

  List<ITestResult> runInSequence(
      TestMethodArguments arguments,
      ITestInvoker testInvoker,
      ITestContext context,
      AtomicInteger invocationCount,
      FailureContext failure,
      Iterator<Object[]> allParameterValues,
      boolean skipFailedInvocationCounts);

  List<ITestResult> runInParallel(
      TestMethodArguments arguments,
      ITestInvoker testInvoker,
      ITestContext context,
      AtomicInteger invocationCount,
      FailureContext failure,
      Iterator<Object[]> allParameterValues,
      boolean skipFailedInvocationCounts);
}
