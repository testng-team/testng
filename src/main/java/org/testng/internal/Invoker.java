package org.testng.internal;

import org.testng.*;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.thread.ThreadUtil;
import org.testng.internal.thread.graph.IWorker;
import org.testng.xml.XmlSuite;

import java.util.*;

import static org.testng.internal.ParameterHandler.ParameterBag;

/**
 * This class is responsible for invoking methods: - test methods - configuration methods - possibly
 * in a separate thread and then for notifying the result listeners.
 *
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class Invoker extends AbstractInvoker {

  public Invoker(
      IConfiguration configuration,
      ITestContext testContext,
      ITestResultNotifier notifier,
      SuiteRunState state,
      boolean skipFailedInvocationCounts,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      List<IClassListener> classListeners,
      Collection<IDataProviderListener> dataProviderListeners) {
      super(configuration, testContext, notifier, state, skipFailedInvocationCounts, invokedMethodListeners,
              classListeners, dataProviderListeners);
  }

  /**
   * invokeTestMethods() eventually converge here to invoke a single @Test method.
   *
   * <p>This method is responsible for actually invoking the method. It decides if the invocation
   * must be done:
   *
   * <ul>
   *   <li>through an <code>IHookable</code>
   *   <li>directly (through reflection)
   *   <li>in a separate thread (in case it needs to timeout)
   * </ul>
   *
   * <p>This method is also responsible for
   * invoking @BeforeGroup, @BeforeMethod, @AfterMethod, @AfterGroup if it is the case for the
   * passed in @Test method.
   */
  ITestResult invokeTestMethod(
      Object instance,
      ITestNGMethod tm,
      Object[] parameterValues,
      int parametersIndex,
      XmlSuite suite,
      Map<String, String> params,
      ITestClass testClass,
      ITestNGMethod[] beforeMethods,
      ITestNGMethod[] afterMethods,
      ConfigurationGroupMethods groupMethods,
      FailureContext failureContext) {
    // Mark this method with the current thread id
    tm.setId(ThreadUtil.currentThreadInfo());

    return invokeMethod(
        instance,
        tm,
        parameterValues,
        parametersIndex,
        suite,
        params,
        testClass,
        beforeMethods,
        afterMethods,
        groupMethods,
        failureContext);
  }

  /**
   * Invoke all the test methods. Note the plural: the method passed in parameter might be invoked
   * several times if the test class it belongs to has more than one instance (i.e., if an @Factory
   * method has been declared somewhere that returns several instances of this TestClass). If
   * no @Factory method was specified, testMethod will only be invoked once.
   *
   * <p>Note that this method also takes care of invoking the beforeTestMethod and afterTestMethod,
   * if any.
   *
   * <p>Note (alex): this method can be refactored to use a SingleTestMethodWorker that directly
   * invokes {@link #invokeTestMethod(Object, ITestNGMethod, Object[], int, XmlSuite, Map,
   * ITestClass, ITestNGMethod[], ITestNGMethod[], ConfigurationGroupMethods, FailureContext)} and
   * this would simplify the implementation (see how DataTestMethodWorker is used)
   */
  @Override
  public List<ITestResult> invokeTestMethods(
      ITestNGMethod testMethod,
      Map<String, String> testParameters,
      ConfigurationGroupMethods groupMethods,
      Object instance,
      ITestContext testContext) {
    // Potential bug here if the test method was declared on a parent class
    if (testMethod.getTestClass() == null) {
      throw new IllegalArgumentException(
          "COULDN'T FIND TESTCLASS FOR " + testMethod.getRealClass());
    }
    XmlSuite suite = testContext.getSuite().getXmlSuite();

    if (!MethodHelper.isEnabled(
        testMethod.getConstructorOrMethod().getMethod(), getAnnotationFinder())) {
      // return if the method is not enabled. No need to do any more calculations
      return Collections.emptyList();
    }

    // By the time this testMethod to be invoked,
    // all dependencies should be already run or we need to skip this method,
    // so invocation count should not affect dependencies check
    String okToProceed = checkDependencies(testMethod, testContext.getAllTestMethods());

    if (okToProceed != null) {
      //
      // Not okToProceed. Test is being skipped
      //
      ITestResult result =
          registerSkippedTestResult(
              testMethod, System.currentTimeMillis(), new Throwable(okToProceed));
      getResultNotifier().addSkippedTest(testMethod, result);
      return Collections.singletonList(result);
    }

    Map<String, String> parameters =
        testMethod.findMethodParameters(testContext.getCurrentXmlTest());

    // For invocationCount > 1 and threadPoolSize > 1 run this method in its own pool thread.
    if (testMethod.getInvocationCount() > 1 && testMethod.getThreadPoolSize() > 1) {
      return invokePooledTestMethods(testMethod, suite, parameters, groupMethods, testContext);
    }

    long timeOutInvocationCount = testMethod.getInvocationTimeOut();
    // FIXME: Is this correct?
    boolean onlyOne = testMethod.getThreadPoolSize() > 1 || timeOutInvocationCount > 0;

    int invocationCount = onlyOne ? 1 : testMethod.getInvocationCount();

    ITestClass testClass = testMethod.getTestClass();
    List<ITestResult> result = Lists.newArrayList();
    FailureContext failure = new FailureContext();
    ITestNGMethod[] beforeMethods =
        TestNgMethodUtils.filterBeforeTestMethods(testClass, CAN_RUN_FROM_CLASS);
    ITestNGMethod[] afterMethods =
        TestNgMethodUtils.filterAfterTestMethods(testClass, CAN_RUN_FROM_CLASS);
    while (invocationCount-- > 0) {
      long start = System.currentTimeMillis();

      Map<String, String> allParameterNames = Maps.newHashMap();
      ParameterHandler handler = new ParameterHandler(getAnnotationFinder(), getDataProviderListeners());

      ParameterBag bag =
          handler.createParameters(
              testMethod, parameters, allParameterNames, testContext, instance);

      if (bag.hasErrors()) {
        ITestResult tr = bag.errorResult;
        Throwable throwable = tr.getThrowable();
        if (throwable instanceof TestNGException) {
          tr.setStatus(ITestResult.FAILURE);
          getResultNotifier().addFailedTest(testMethod, tr);
        } else {
          tr.setStatus(ITestResult.SKIP);
          getResultNotifier().addSkippedTest(testMethod, tr);
        }
        runTestListeners(tr);
        result.add(tr);
        continue;
      }

      Iterator<Object[]> allParameterValues = bag.parameterHolder.parameters;
      int parametersIndex = 0;

      try {

        if (bag.runInParallel()) {
          List<TestMethodWithDataProviderMethodWorker> workers = Lists.newArrayList();
          while (allParameterValues.hasNext()) {
            Object[] next = allParameterValues.next();
            if (next == null) {
              // skipped value
              parametersIndex++;
              continue;
            }
            Object[] parameterValues =
                Parameters.injectParameters(
                    next, testMethod.getConstructorOrMethod().getMethod(), testContext);

            TestMethodWithDataProviderMethodWorker w =
                new TestMethodWithDataProviderMethodWorker(
                    this,
                    testMethod,
                    parametersIndex,
                    parameterValues,
                    instance,
                    parameters,
                    testClass,
                    beforeMethods,
                    afterMethods,
                    groupMethods,
                    testContext,
                    isSkipFailedInvocationCounts(),
                    invocationCount,
                    failure.count,
                    getResultNotifier());
            workers.add(w);
            // testng387: increment the param index in the bag.
            parametersIndex++;
          }
          PoolService<List<ITestResult>> ps = new PoolService<>(suite.getDataProviderThreadCount());
          List<List<ITestResult>> r = ps.submitTasksAndWait(workers);
          for (List<ITestResult> l2 : r) {
            result.addAll(l2);
          }

        } else {
          while (allParameterValues.hasNext()) {
            Object[] next = allParameterValues.next();
            if (next == null) {
              // skipped value
              parametersIndex++;
              continue;
            }
            Object[] parameterValues =
                Parameters.injectParameters(
                    next, testMethod.getConstructorOrMethod().getMethod(), testContext);

            List<ITestResult> tmpResults = Lists.newArrayList();
            int tmpResultsIndex = -1;
            try {
              tmpResults.add(
                  invokeTestMethod(
                      instance,
                      testMethod,
                      parameterValues,
                      parametersIndex,
                      suite,
                      parameters,
                      testClass,
                      beforeMethods,
                      afterMethods,
                      groupMethods,
                      failure));
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

                failure =
                    retryFailed(
                        instance,
                        testMethod,
                        parameterValues,
                        testClass,
                        beforeMethods,
                        afterMethods,
                        groupMethods,
                        retryResults,
                        failure.count,
                        testContext,
                        parameters,
                        parametersIndex);
                result.addAll(retryResults);
              }

              // If we have a failure, skip all the
              // other invocationCounts
              if (failure.count > 0
                  && (isSkipFailedInvocationCounts() || testMethod.skipFailedInvocations())) {
                while (invocationCount-- > 0) {
                  result.add(
                      registerSkippedTestResult(testMethod, System.currentTimeMillis(), null));
                }
              }
            } // end finally
            parametersIndex++;
          }
        }
      } catch (Throwable cause) {
        ITestResult r =
            new TestResult(
                testMethod.getTestClass(),
                testMethod,
                cause,
                start,
                System.currentTimeMillis(),
                getTestContext());
        r.setStatus(TestResult.FAILURE);
        result.add(r);
        runTestListeners(r);
        getResultNotifier().addFailedTest(testMethod, r);
      } // catch
    }

    return result;
  }

  /** Invokes a method that has a specified threadPoolSize. */
  protected List<ITestResult> invokePooledTestMethods(
          ITestNGMethod testMethod,
          XmlSuite suite,
          Map<String, String> parameters,
          ConfigurationGroupMethods groupMethods,
          ITestContext testContext) {
    //
    // Create the workers
    //
    List<IWorker<ITestNGMethod>> workers = Lists.newArrayList();

    // Create one worker per invocationCount
    for (int i = 0; i < testMethod.getInvocationCount(); i++) {
      // we use clones for reporting purposes
      ITestNGMethod clonedMethod = testMethod.clone();
      clonedMethod.setInvocationCount(1);
      clonedMethod.setThreadPoolSize(1);

      MethodInstance mi = new MethodInstance(clonedMethod);
      workers.add(new SingleTestMethodWorker(this, mi, parameters, testContext, getClassListeners()));
    }

    return runWorkers(
            testMethod, workers, testMethod.getThreadPoolSize(), groupMethods, suite, parameters);
  }

  /**
   * To reduce thread contention and also to correctly handle thread-confinement this method invokes
   * the @BeforeGroups and @AfterGroups corresponding to the current @Test method.
   */
  private List<ITestResult> runWorkers(
          ITestNGMethod testMethod,
          List<IWorker<ITestNGMethod>> workers,
          int threadPoolSize,
          ConfigurationGroupMethods groupMethods,
          XmlSuite suite,
          Map<String, String> parameters) {
    // Invoke @BeforeGroups on the original method (reduce thread contention,
    // and also solve thread confinement)
    ITestClass testClass = testMethod.getTestClass();
    Object[] instances = testClass.getInstances(true);
    for (Object instance : instances) {
      invokeBeforeGroupsConfigurations(testMethod, groupMethods, suite, parameters, instance);
    }

    long maxTimeOut = -1; // 10 seconds

    for (IWorker<ITestNGMethod> tmw : workers) {
      long mt = tmw.getTimeOut();
      if (mt > maxTimeOut) {
        maxTimeOut = mt;
      }
    }

    ThreadUtil.execute("methods", workers, threadPoolSize, maxTimeOut, true);

    //
    // Collect all the TestResults
    //
    List<ITestResult> result = Lists.newArrayList();
    for (IWorker<ITestNGMethod> tmw : workers) {
      if (tmw instanceof TestMethodWorker) {
        result.addAll(((TestMethodWorker) tmw).getTestResults());
      }
    }

    for (Object instance : instances) {
      invokeAfterGroupsConfigurations(testMethod, groupMethods, suite, parameters, instance);
    }

    return result;
  }
}
