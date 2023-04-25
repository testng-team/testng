package org.testng.internal.invokers;

import static org.testng.internal.invokers.InvokedMethodListenerMethod.AFTER_INVOCATION;
import static org.testng.internal.invokers.InvokedMethodListenerMethod.BEFORE_INVOCATION;
import static org.testng.internal.invokers.Invoker.CAN_RUN_FROM_CLASS;
import static org.testng.internal.invokers.ParameterHandler.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.testng.DataProviderHolder;
import org.testng.DataProviderInvocationException;
import org.testng.IClassListener;
import org.testng.IDataProviderListener;
import org.testng.IHookable;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IRetryAnalyzer;
import org.testng.ISuite;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.testng.SuiteRunState;
import org.testng.SuiteRunner;
import org.testng.TestException;
import org.testng.TestNGException;
import org.testng.collections.CollectionUtils;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.*;
import org.testng.internal.invokers.GroupConfigMethodArguments.Builder;
import org.testng.internal.invokers.InvokeMethodRunnable.TestNGRuntimeException;
import org.testng.internal.thread.ThreadExecutionException;
import org.testng.internal.thread.ThreadUtil;
import org.testng.thread.IWorker;
import org.testng.xml.XmlSuite;

class TestInvoker extends BaseInvoker implements ITestInvoker {

  private final ConfigInvoker invoker;
  private final DataProviderHolder holder;
  private final List<IClassListener> m_classListeners;
  private final boolean m_skipFailedInvocationCounts;

  public TestInvoker(
      ITestResultNotifier m_notifier,
      ITestContext m_testContext,
      SuiteRunState m_suiteState,
      IConfiguration m_configuration,
      Collection<IInvokedMethodListener> m_invokedMethodListeners,
      DataProviderHolder holder,
      List<IClassListener> m_classListeners,
      boolean m_skipFailedInvocationCounts,
      ConfigInvoker invoker) {
    super(m_notifier, m_invokedMethodListeners, m_testContext, m_suiteState, m_configuration);
    this.holder = holder;
    this.m_classListeners = m_classListeners;
    this.m_skipFailedInvocationCounts = m_skipFailedInvocationCounts;
    this.invoker = invoker;
  }

  @Override
  public ITestResultNotifier getNotifier() {
    return m_notifier;
  }

  public List<ITestResult> invokeTestMethods(
      ITestNGMethod testMethod,
      ConfigurationGroupMethods groupMethods,
      Object instance,
      ITestContext context) {
    // Potential bug here if the test method was declared on a parent class
    if (testMethod.getTestClass() == null) {
      throw new IllegalArgumentException(
          "COULDN'T FIND TESTCLASS FOR " + testMethod.getRealClass());
    }

    if (!MethodHelper.isEnabled(
        testMethod.getConstructorOrMethod().getMethod(), annotationFinder())) {
      // return if the method is not enabled. No need to do any more calculations
      return Collections.emptyList();
    }

    Map<String, String> parameters = testMethod.findMethodParameters(context.getCurrentXmlTest());
    // By the time this testMethod to be invoked,
    // all dependencies should be already run or we need to skip this method,
    // so invocation count should not affect dependencies check
    String okToProceed = checkDependencies(testMethod);

    if (okToProceed != null) {
      //
      // Not okToProceed. Test is being skipped
      //
      List<ITestResult> results = new ArrayList<>();
      Consumer<ITestResult> resultProcessor =
          result -> {
            m_notifier.addSkippedTest(testMethod, result);
            InvokedMethod invokedMethod = new InvokedMethod(System.currentTimeMillis(), result);
            invokeListenersForSkippedTestResult(result, invokedMethod);
          };
      boolean reportAllDataDrivenTestsAsSkipped =
          m_configuration.getReportAllDataDrivenTestsAsSkipped();
      if (reportAllDataDrivenTestsAsSkipped && testMethod.isDataDriven()) {
        ParameterHandler handler =
            new ParameterHandler(
                m_configuration.getObjectFactory(),
                annotationFinder(),
                buildDataProviderHolder(),
                1);

        ParameterBag bag =
            handler.createParameters(
                testMethod, Maps.newHashMap(), Maps.newHashMap(), context, instance);
        Iterator<Object[]> allParamValues = Objects.requireNonNull(bag.parameterHolder).parameters;
        Iterable<Object[]> allParameterValues = CollectionUtils.asIterable(allParamValues);
        for (Object[] next : allParameterValues) {
          if (next == null) {
            continue;
          }
          Method m = testMethod.getConstructorOrMethod().getMethod();
          Object[] parameterValues = Parameters.injectParameters(next, m, context);
          ITestResult result =
              registerSkippedTestResult(
                  testMethod, System.currentTimeMillis(), new Throwable(okToProceed));
          result.setParameters(parameterValues);
          resultProcessor.accept(result);
          results.add(result);
        }
      } else {
        ITestResult result =
            registerSkippedTestResult(
                testMethod, System.currentTimeMillis(), new Throwable(okToProceed));
        resultProcessor.accept(result);
        results.add(result);
      }
      testMethod.incrementCurrentInvocationCount();
      GroupConfigMethodArguments args =
          new Builder()
              .forTestMethod(testMethod)
              .withGroupConfigMethods(groupMethods)
              .forInstance(instance)
              .withParameters(parameters)
              .build();
      this.invoker.invokeAfterGroupsConfigurations(args);
      return Collections.unmodifiableList(results);
    }

    // For invocationCount > 1 and threadPoolSize > 1 run this method in its own pool thread.
    if (testMethod.getInvocationCount() > 1 && testMethod.getThreadPoolSize() > 1) {
      return invokePooledTestMethods(testMethod, parameters, groupMethods, context);
    }

    long timeOutInvocationCount = testMethod.getInvocationTimeOut();
    // FIXME: Is this correct?
    boolean onlyOne = testMethod.getThreadPoolSize() > 1 || timeOutInvocationCount > 0;

    ITestClass testClass = testMethod.getTestClass();
    ITestNGMethod[] beforeMethods =
        TestNgMethodUtils.filterBeforeTestMethods(testClass, CAN_RUN_FROM_CLASS);
    ITestNGMethod[] afterMethods =
        TestNgMethodUtils.filterAfterTestMethods(testClass, CAN_RUN_FROM_CLASS);
    int invocationCount = onlyOne ? 1 : testMethod.getInvocationCount();

    TestMethodArguments arguments =
        new TestMethodArguments.Builder()
            .usingInstance(instance)
            .forTestMethod(testMethod)
            .withParameters(parameters)
            .forTestClass(testClass)
            .usingBeforeMethods(beforeMethods)
            .usingAfterMethods(afterMethods)
            .usingGroupMethods(groupMethods)
            .build();
    MethodInvocationAgent agent = new MethodInvocationAgent(arguments, this, context);
    while (invocationCount-- > 0) {
      invocationCount = agent.invoke(invocationCount);
    }

    return agent.getResult();
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
  public ITestResult invokeTestMethod(
      TestMethodArguments arguments, XmlSuite suite, FailureContext failureContext) {
    // Mark this method with the current thread id
    arguments.getTestMethod().setId(ThreadUtil.currentThreadInfo());

    return invokeMethod(arguments, suite, failureContext);
  }

  public FailureContext retryFailed(
      TestMethodArguments arguments,
      List<ITestResult> result,
      int failureCount,
      ITestContext testContext) {
    FailureContext failure = new FailureContext();
    failure.count = failureCount;
    failure.representsRetriedMethod = true;
    do {
      failure.instances = Lists.newArrayList();
      Map<String, String> allParameters = Maps.newHashMap();
      int verbose = testContext.getCurrentXmlTest().getVerbose();
      // TODO: This recreates all the parameters every time when we only need
      // one specific set. Should optimize it by only recreating the set needed.
      ParameterHandler handler =
          new ParameterHandler(
              m_configuration.getObjectFactory(), annotationFinder(), this.holder, verbose);

      ParameterBag bag =
          handler.createParameters(
              arguments.getTestMethod(), arguments.getParameters(), allParameters, testContext);
      ITestResult errorResult = bag.errorResult;

      if (errorResult != null) {
        Throwable cause = errorResult.getThrowable();
        String m = errorResult.getMethod().getMethodName();
        String msg =
            String.format(
                "Encountered problems when gathering parameter values for [%s]. Root cause: ", m);
        throw new DataProviderInvocationException(msg, cause);
      }
      Object[] parameterValues = arguments.getParameterValues();
      TestMethodArguments tma =
          new TestMethodArguments.Builder()
              .usingArguments(arguments)
              .withParameterValues(parameterValues)
              .withParameters(allParameters)
              .build();

      result.add(invokeMethod(tma, testContext.getSuite().getXmlSuite(), failure));
    } while (!failure.instances.isEmpty());
    return failure;
  }

  public void runTestResultListener(ITestResult tr) {
    // For onTestStart method, still run as insert order
    // but regarding
    // onTestSkipped/onTestFailedButWithinSuccessPercentage/onTestFailedWithTimeout/onTestFailure/onTestSuccess, it should be reverse order.
    boolean isFinished = tr.getStatus() != ITestResult.STARTED;
    List<ITestListener> listeners =
        isFinished
            ? Lists.newReversedArrayList(m_notifier.getTestListeners())
            : m_notifier.getTestListeners();
    TestListenerHelper.runTestListeners(tr, listeners);
  }

  private Collection<IDataProviderListener> dataProviderListeners() {
    ISuite suite = this.m_testContext.getSuite();
    Collection<IDataProviderListener> dpListeners = Sets.newHashSet(this.holder.getListeners());
    if (suite instanceof SuiteRunner) {
      Collection<IDataProviderListener> listeners =
          ((SuiteRunner) suite).getDataProviderListeners();
      dpListeners.addAll(listeners);
    }
    return dpListeners;
  }

  private DataProviderHolder buildDataProviderHolder() {
    DataProviderHolder holder = new DataProviderHolder();
    holder.addListeners(dataProviderListeners());
    holder.addInterceptors(this.holder.getInterceptors());
    return holder;
  }

  /**
   * Checks to see of the test method has certain dependencies that prevents TestNG from executing
   * it
   *
   * @param testMethod test method being checked for
   * @return error message or null if dependencies have been run successfully
   */
  private String checkDependencies(ITestNGMethod testMethod) {
    // If this method is marked alwaysRun, no need to check for its dependencies
    if (testMethod.isAlwaysRun()) {
      return null;
    }

    // Any missing group?
    if (testMethod.getMissingGroup() != null && !testMethod.ignoreMissingDependencies()) {
      return "Method "
          + testMethod
          + " depends on nonexistent group \""
          + testMethod.getMissingGroup()
          + "\"";
    }

    // If this method depends on groups, collect all the methods that
    // belong to these groups and make sure they have been run successfully
    String[] groups = testMethod.getGroupsDependedUpon();
    ITestNGMethod[] allTestMethods = m_testContext.getAllTestMethods();
    if (null != groups && groups.length > 0) {
      // Get all the methods that belong to the group depended upon
      for (String element : groups) {
        ITestNGMethod[] methods =
            MethodGroupsHelper.findMethodsThatBelongToGroup(testMethod, allTestMethods, element);
        if (methods.length == 0 && !testMethod.ignoreMissingDependencies()) {
          // Group is missing
          return "Method " + testMethod + " depends on nonexistent group \"" + element + "\"";
        }
        if (failuresPresentInUpstreamDependency(testMethod, methods)) {
          return "Method "
              + testMethod
              + " depends on not successfully finished methods in group \""
              + element
              + "\"";
        }
      }
    } // depends on groups

    // If this method depends on other methods, make sure all these other
    // methods have been run successfully
    if (TestNgMethodUtils.cannotRunMethodIndependently(testMethod)) {
      ITestNGMethod[] methods = MethodHelper.findDependedUponMethods(testMethod, allTestMethods);

      if (failuresPresentInUpstreamDependency(testMethod, methods)) {
        String methodsInfo =
            Arrays.stream(methods)
                .map(tm -> tm.getQualifiedName() + "() on instance " + tm.getInstance().toString())
                .collect(Collectors.joining("\n"));
        return String.format(
            "Method %s() on instance %s depends on not successfully finished methods \n[%s]",
            testMethod.getQualifiedName(), testMethod.getInstance().toString(), methodsInfo);
      }
    }

    return null;
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
      Map<String, String> parameters) {
    // Invoke @BeforeGroups on the original method (reduce thread contention,
    // and also solve thread confinement)
    ITestClass testClass = testMethod.getTestClass();
    Object[] instances = testClass.getInstances(true);
    for (Object instance : instances) {
      GroupConfigMethodArguments arguments =
          new GroupConfigMethodArguments.Builder()
              .forTestMethod(testMethod)
              .withGroupConfigMethods(groupMethods)
              .withParameters(parameters)
              .forInstance(instance)
              .build();
      invoker.invokeBeforeGroupsConfigurations(arguments);
    }

    long maxTimeOut =
        workers.parallelStream().map(IWorker::getTimeOut).max(Long::compare).orElse(-1L);

    ThreadUtil.execute("methods", workers, threadPoolSize, maxTimeOut);

    //
    // Collect all the TestResults
    //
    List<ITestResult> result =
        workers
            .parallelStream()
            .filter(tmw -> tmw instanceof TestMethodWorker)
            .flatMap(tmw -> ((TestMethodWorker) tmw).getTestResults().stream())
            .collect(Collectors.toList());

    for (Object instance : instances) {
      GroupConfigMethodArguments arguments =
          new GroupConfigMethodArguments.Builder()
              .forTestMethod(testMethod)
              .withGroupConfigMethods(groupMethods)
              .withParameters(parameters)
              .forInstance(instance)
              .build();
      invoker.invokeAfterGroupsConfigurations(arguments);
    }

    return result;
  }

  private boolean failuresPresentInUpstreamDependency(
      ITestNGMethod testMethod, ITestNGMethod[] methods) {
    // Make sure the method has been run successfully
    for (ITestNGMethod method : methods) {
      Set<ITestResult> results = keepSameInstances(testMethod, m_notifier.getPassedTests(method));
      Set<ITestResult> failedAndSkippedMethods = Sets.newHashSet();
      Set<ITestResult> skippedAttempts = m_notifier.getSkippedTests(method);
      failedAndSkippedMethods.addAll(m_notifier.getFailedTests(method));
      failedAndSkippedMethods.addAll(skippedAttempts);
      Set<ITestResult> failedresults = keepSameInstances(testMethod, failedAndSkippedMethods);
      boolean wasMethodRetried = !results.isEmpty() && !skippedAttempts.isEmpty();

      if (!wasMethodRetried && !failedresults.isEmpty()) {
        // If failed results were returned on the same instance, then these tests didn't pass
        return true;
      }

      Optional<ITestResult> found =
          results.parallelStream().filter(testResult -> !testResult.isSuccess()).findAny();
      if (found.isPresent()) {
        return true;
      }
    }
    return false;
  }

  /** @return the test results that apply to one of the instances of the testMethod. */
  private Set<ITestResult> keepSameInstances(ITestNGMethod method, Set<ITestResult> results) {
    return results
        .parallelStream()
        .filter(
            r -> {
              Object instance =
                  Optional.ofNullable(r.getInstance()).orElse(r.getMethod().getInstance());
              if (method.getGroupsDependedUpon().length == 0) {
                // Consider equality of objects alone if we are NOT dealing with group dependency.
                return instance == method.getInstance();
              }
              // Keep this instance if
              // 1) It's on a different class or
              // 2) It's on the same class and on the same instance
              boolean unEqualTestClasses =
                  !r.getTestClass().getRealClass().equals(method.getTestClass().getRealClass());
              boolean sameInstance = instance == method.getInstance();
              return sameInstance || unEqualTestClasses;
            })
        .collect(Collectors.toSet());
  }

  /** Invokes a method that has a specified threadPoolSize. */
  private List<ITestResult> invokePooledTestMethods(
      ITestNGMethod testMethod,
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
      workers.add(
          new SingleTestMethodWorker(this, invoker, mi, parameters, testContext, m_classListeners));
    }

    return runWorkers(
        testMethod, workers, testMethod.getThreadPoolSize(), groupMethods, parameters);
  }

  private void collectResults(ITestNGMethod testMethod, ITestResult result) {
    // Collect the results
    int status = result.getStatus();
    if (ITestResult.SUCCESS == status) {
      m_notifier.addPassedTest(testMethod, result);
    } else if (ITestResult.SKIP == status) {
      m_notifier.addSkippedTest(testMethod, result);
    } else if (ITestResult.FAILURE == status) {
      m_notifier.addFailedTest(testMethod, result);
    } else if (ITestResult.SUCCESS_PERCENTAGE_FAILURE == status) {
      m_notifier.addFailedButWithinSuccessPercentageTest(testMethod, result);
    } else {
      assert false : "UNKNOWN STATUS:" + status;
    }
  }

  public void invokeListenersForSkippedTestResult(ITestResult r, IInvokedMethod invokedMethod) {
    if (m_configuration.alwaysRunListeners()) {
      runInvokedMethodListeners(BEFORE_INVOCATION, invokedMethod, r);
      runInvokedMethodListeners(AFTER_INVOCATION, invokedMethod, r);
    }
    runTestResultListener(r);
  }

  private static void setTestStatus(ITestResult result, int status) {
    // set the test to success as long as the testResult hasn't been changed by the user via
    // Reporter.getCurrentTestResult
    if (result.getStatus() == ITestResult.STARTED) {
      result.setStatus(status);
    }
  }

  private static class StatusHolder {

    boolean handled = false;
    int originalStatus;
    int status;
  }

  private void handleInvocationResult(
      ITestNGMethod testMethod,
      ITestResult testResult,
      FailureContext failure,
      StatusHolder holder,
      boolean willRetry) {

    if (willRetry) {
      Object instance = testResult.getInstance();
      if (!failure.instances.contains(instance)) {
        failure.instances.add(instance);
      }
      testResult.setStatus(ITestResult.SKIP);
      testResult.setWasRetried(true);
    } else {
      testResult.setStatus(holder.status);
      if (holder.status == ITestResult.FAILURE && !holder.handled) {
        int count = failure.count++;
        if (testMethod.isDataDriven()) {
          count = 0;
        }
        handleException(testResult.getThrowable(), testMethod, testResult, count);
      }
    }
  }

  private boolean shouldRetryTestMethod(
      ITestNGMethod testMethod,
      ITestResult testResult,
      FailureContext failure,
      StatusHolder holder) {
    IRetryAnalyzer retryAnalyzer = testMethod.getRetryAnalyzer(testResult);

    return retryAnalyzer != null
        && holder.status == ITestResult.FAILURE
        && failure.instances != null
        && retryAnalyzer.retry(testResult);
  }

  // pass both paramValues and paramIndex to be thread safe in case parallel=true + dataprovider.
  private ITestResult invokeMethod(
      TestMethodArguments arguments, XmlSuite suite, FailureContext failureContext) {
    TestResult testResult = TestResult.newEmptyTestResult();
    testResult.setParameters(arguments.getParameterValues());
    testResult.setParameterIndex(arguments.getParametersIndex());
    testResult.setHost(m_testContext.getHost());

    GroupConfigMethodArguments cfgArgs =
        new GroupConfigMethodArguments.Builder()
            .forTestMethod(arguments.getTestMethod())
            .withGroupConfigMethods(arguments.getGroupMethods())
            .withParameters(arguments.getParameters())
            .forInstance(arguments.getInstance())
            .build();
    invoker.invokeBeforeGroupsConfigurations(cfgArgs);

    ITestNGMethod[] setupConfigMethods =
        TestNgMethodUtils.filterSetupConfigurationMethods(
            arguments.getTestMethod(), arguments.getBeforeMethods());
    runConfigMethods(arguments, suite, testResult, setupConfigMethods);

    long startTime = System.currentTimeMillis();
    InvokedMethod invokedMethod = new InvokedMethod(startTime, testResult);

    if (!failureContext.representsRetriedMethod
        && invoker.hasConfigurationFailureFor(
            arguments.getTestMethod(),
            arguments.getTestMethod().getGroups(),
            arguments.getTestClass(),
            arguments.getInstance())) {
      Throwable exception =
          ExceptionUtils.getExceptionDetails(m_testContext, arguments.getInstance());
      ITestResult result =
          registerSkippedTestResult(
              arguments.getTestMethod(), System.currentTimeMillis(), exception, testResult);
      result.setParameters(testResult.getParameters());
      TestResult.copyAttributes(testResult, result);
      m_notifier.addSkippedTest(arguments.getTestMethod(), result);
      arguments.getTestMethod().incrementCurrentInvocationCount();
      testResult.setMethod(arguments.getTestMethod());
      invokedMethod = new InvokedMethod(startTime, result);
      invokeListenersForSkippedTestResult(result, invokedMethod);
      runAfterConfigurations(arguments, suite, testResult);
      runAfterGroupsConfigurations(arguments);

      return result;
    }

    //
    // Create the ExtraOutput for this method
    //
    try {
      testResult =
          TestResult.newTestResultFrom(
              testResult, arguments.getTestMethod(), m_testContext, System.currentTimeMillis());
      // Recreate the invoked method object again, because we now have a new test result object
      invokedMethod = new InvokedMethod(invokedMethod.getDate(), testResult);

      testResult.setStatus(ITestResult.STARTED);

      Reporter.setCurrentTestResult(testResult);

      // Fix from ansgarkonermann
      // invokedMethod is used in the finally, which can be invoked if
      // any of the test listeners throws an exception, therefore,
      // invokedMethod must have a value before we get here
      if (!m_suiteState.isFailed()) {
        runTestResultListener(testResult);
      }

      log(3, "Invoking " + arguments.getTestMethod().getQualifiedName());
      runInvokedMethodListeners(BEFORE_INVOCATION, invokedMethod, testResult);
      if (testResult.getStatus() == ITestResult.SKIP) {
        return testResult;
      }

      if (arguments.getTestMethod() instanceof IInvocationStatus) {
        ((IInvocationStatus) arguments.getTestMethod()).setInvokedAt(invokedMethod.getDate());
      }

      Method thisMethod = arguments.getTestMethod().getConstructorOrMethod().getMethod();

      if (RuntimeBehavior.isDryRun()) {
        setTestStatus(testResult, ITestResult.SUCCESS);
        return testResult;
      }

      // If this method is a IHookable, invoke its run() method
      IHookable hookableInstance =
          IHookable.class.isAssignableFrom(arguments.getTestMethod().getRealClass())
              ? (IHookable) arguments.getInstance()
              : m_configuration.getHookable();

      if (MethodHelper.calculateTimeOut(arguments.getTestMethod()) <= 0) {
        if (hookableInstance != null) {
          MethodInvocationHelper.invokeHookable(
              arguments.getInstance(),
              arguments.getParameterValues(),
              hookableInstance,
              thisMethod,
              testResult);
        } else {
          // Not a IHookable, invoke directly
          MethodInvocationHelper.invokeMethod(
              thisMethod, arguments.getInstance(), arguments.getParameterValues());
        }
        setTestStatus(testResult, ITestResult.SUCCESS);
      } else {
        // Method with a timeout
        MethodInvocationHelper.invokeWithTimeout(
            arguments.getTestMethod(),
            arguments.getInstance(),
            arguments.getParameterValues(),
            testResult,
            hookableInstance);
      }
    } catch (InvocationTargetException ite) {
      testResult.setThrowable(ite.getCause());
      setTestStatus(testResult, ITestResult.FAILURE);
    } catch (ThreadExecutionException tee) { // wrapper for TestNGRuntimeException
      Throwable cause = tee.getCause();
      if (TestNGRuntimeException.class.equals(cause.getClass())) {
        testResult.setThrowable(cause.getCause());
      } else {
        testResult.setThrowable(cause);
      }
      setTestStatus(testResult, ITestResult.FAILURE);
    } catch (Throwable thr) { // covers the non-wrapper exceptions
      testResult.setThrowable(thr);
      int status = ITestResult.FAILURE;
      if (thr instanceof SkipException) {
        status = ITestResult.SKIP;
      }
      setTestStatus(testResult, status);
    } finally {
      // Set end time ASAP
      testResult.setEndMillis(System.currentTimeMillis());
      cleanInterruptStatus();
      ExpectedExceptionsHolder expectedExceptionClasses =
          new ExpectedExceptionsHolder(
              annotationFinder(),
              arguments.getTestMethod(),
              new RegexpExpectedExceptionsHolder(annotationFinder(), arguments.getTestMethod()));
      StatusHolder holder =
          considerExceptions(
              arguments.getTestMethod(), testResult, expectedExceptionClasses, failureContext);
      runInvokedMethodListeners(AFTER_INVOCATION, invokedMethod, testResult);
      updateStatusHolderAccordingToTestResult(testResult, holder);
      boolean willRetryMethod =
          shouldRetryTestMethod(arguments.getTestMethod(), testResult, failureContext, holder);
      handleInvocationResult(
          arguments.getTestMethod(), testResult, failureContext, holder, willRetryMethod);

      // If this method has a data provider and just failed, memorize the number
      // at which it failed.
      // Note: we're not exactly testing that this method has a data provider, just
      // that it has parameters, so might have to revisit this if bugs get reported
      // for the case where this method has parameters that don't come from a data
      // provider
      if (testResult.getThrowable() != null
          && (arguments.getParameterValues().length > 0
              || testResult.getFactoryParameters().length > 0)) {
        int parametersIndex = arguments.getParametersIndex();
        if (null != testResult.getMethod().getFactoryMethodParamsInfo()) {
          parametersIndex = testResult.getMethod().getFactoryMethodParamsInfo().getIndex();
        }
        arguments.getTestMethod().addFailedInvocationNumber(parametersIndex);
      }

      //
      // Increment the invocation count for this method
      //
      arguments.getTestMethod().incrementCurrentInvocationCount();

      runTestResultListener(testResult);

      collectResults(arguments.getTestMethod(), testResult);

      runAfterConfigurations(arguments, suite, testResult);
      if (!willRetryMethod) {
        runAfterGroupsConfigurations(arguments);
      }

      // Reset the test result last. If we do this too early, Reporter.log()
      // invocations from listeners will be discarded
      Reporter.setCurrentTestResult(null);
    }

    return testResult;
  }

  private static void cleanInterruptStatus() {
    if (Thread.currentThread().isInterrupted()) {
      Thread.interrupted();
    }
  }

  private void runAfterConfigurations(
      TestMethodArguments arguments, XmlSuite suite, TestResult testResult) {
    ITestNGMethod[] teardownConfigMethods =
        TestNgMethodUtils.filterTeardownConfigurationMethods(
            arguments.getTestMethod(), arguments.getAfterMethods());
    runConfigMethods(arguments, suite, testResult, teardownConfigMethods);
  }

  private void runAfterGroupsConfigurations(TestMethodArguments arguments) {
    GroupConfigMethodArguments grpArgs =
        new GroupConfigMethodArguments.Builder()
            .forTestMethod(arguments.getTestMethod())
            .withGroupConfigMethods(arguments.getGroupMethods())
            .withParameters(arguments.getParameters())
            .forInstance(arguments.getInstance())
            .build();

    invoker.invokeAfterGroupsConfigurations(grpArgs);
  }

  private void runConfigMethods(
      TestMethodArguments arguments,
      XmlSuite suite,
      TestResult testResult,
      ITestNGMethod[] teardownConfigMethods) {
    ConfigMethodArguments cfgArgs =
        new ConfigMethodArguments.Builder()
            .forTestClass(arguments.getTestClass())
            .forTestMethod(arguments.getTestMethod())
            .usingConfigMethodsAs(teardownConfigMethods)
            .forSuite(suite)
            .usingParameters(arguments.getParameters())
            .usingParameterValues(arguments.getParameterValues())
            .usingInstance(arguments.getInstance())
            .withResult(testResult)
            .build();
    invoker.invokeConfigurations(cfgArgs);
  }

  @Override
  public ITestResult registerSkippedTestResult(
      ITestNGMethod testMethod, long start, Throwable throwable, ITestResult source) {
    ITestResult result =
        TestResult.newEndTimeAwareTestResult(testMethod, m_testContext, throwable, start);
    if (source != null) {
      TestResult.copyAttributes(source, result);
      result.setParameters(source.getParameters());
    }
    result.setStatus(ITestResult.STARTED);
    runTestResultListener(result);
    result.setStatus(TestResult.SKIP);
    Reporter.setCurrentTestResult(result);
    return result;
  }

  private StatusHolder considerExceptions(
      ITestNGMethod tm,
      ITestResult testResult,
      ExpectedExceptionsHolder exceptionsHolder,
      FailureContext failure) {
    StatusHolder holder = new StatusHolder();
    int status = testResult.getStatus();
    holder.handled = false;

    Throwable ite = testResult.getThrowable();
    if (status == ITestResult.FAILURE && ite != null) {

      //  Invocation caused an exception, see if the method was annotated with @ExpectedException
      if (exceptionsHolder != null) {
        if (exceptionsHolder.isExpectedException(ite)) {
          testResult.setStatus(ITestResult.SUCCESS);
          status = ITestResult.SUCCESS;
        } else {
          if (isSkipExceptionAndSkip(ite)) {
            status = ITestResult.SKIP;
          } else {
            testResult.setThrowable(exceptionsHolder.wrongException(ite));
            status = ITestResult.FAILURE;
          }
        }
      } else {
        handleException(ite, tm, testResult, failure.count++);
        holder.handled = true;
        status = testResult.getStatus();
      }
    } else if (status != ITestResult.SKIP && exceptionsHolder != null) {
      TestException exception = exceptionsHolder.noException(tm);
      if (exception != null) {
        testResult.setThrowable(exception);
        status = ITestResult.FAILURE;
      }
    }
    holder.originalStatus = testResult.getStatus();
    holder.status = status;
    return holder;
  }

  private static void updateStatusHolderAccordingToTestResult(
      ITestResult testResult, StatusHolder holder) {
    if (holder.originalStatus != testResult.getStatus()) {
      holder.status = testResult.getStatus();
    }
  }

  private class MethodInvocationAgent {

    private final ITestContext context;
    private final List<ITestResult> result = Lists.newArrayList();
    private final FailureContext failure = new FailureContext();
    private final ITestInvoker invoker;
    private final TestMethodArguments arguments;

    public MethodInvocationAgent(
        TestMethodArguments arguments, ITestInvoker invoker, ITestContext context) {
      this.arguments = arguments;
      this.invoker = invoker;
      this.context = context;
    }

    public List<ITestResult> getResult() {
      return result;
    }

    public int invoke(int invCount) {
      AtomicInteger invocationCount = new AtomicInteger(invCount);
      long start = System.currentTimeMillis();

      Map<String, String> allParameterNames = Maps.newHashMap();
      int verbose = context.getCurrentXmlTest().getVerbose();
      ParameterHandler handler =
          new ParameterHandler(
              m_configuration.getObjectFactory(),
              annotationFinder(),
              buildDataProviderHolder(),
              verbose);

      ParameterBag bag =
          handler.createParameters(
              arguments.getTestMethod(),
              arguments.getParameters(),
              allParameterNames,
              context,
              arguments.getInstance());

      if (bag.hasErrors()) {
        ITestResult tr = bag.errorResult;
        Throwable throwable = Objects.requireNonNull(tr).getThrowable();
        if (throwable instanceof TestNGException) {
          tr.setStatus(ITestResult.FAILURE);
          m_notifier.addFailedTest(arguments.getTestMethod(), tr);
        } else {
          tr.setStatus(ITestResult.SKIP);
          m_notifier.addSkippedTest(arguments.getTestMethod(), tr);
        }
        runTestResultListener(tr);
        result.add(tr);
        return invocationCount.get();
      }

      Iterator<Object[]> allParameterValues =
          Objects.requireNonNull(bag.parameterHolder).parameters;

      try {

        IMethodRunner runner = this.invoker.getRunner();
        if (bag.runInParallel()) {
          List<ITestResult> parallel =
              runner.runInParallel(
                  arguments,
                  this.invoker,
                  context,
                  invocationCount,
                  failure,
                  allParameterValues,
                  m_skipFailedInvocationCounts);
          result.addAll(parallel);
        } else {
          List<ITestResult> sequential =
              runner.runInSequence(
                  arguments,
                  this.invoker,
                  context,
                  invocationCount,
                  failure,
                  allParameterValues,
                  m_skipFailedInvocationCounts);
          result.addAll(sequential);
        }
      } catch (Throwable cause) {
        ITestResult r =
            TestResult.newEndTimeAwareTestResult(
                arguments.getTestMethod(), m_testContext, cause, start);
        r.setStatus(TestResult.FAILURE);
        result.add(r);
        runTestResultListener(r);
        m_notifier.addFailedTest(arguments.getTestMethod(), r);
      } // catch
      return invocationCount.get();
    }
  }
}
