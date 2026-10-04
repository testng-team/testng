package org.testng;

import static org.testng.internal.Utils.isStringBlank;

import com.google.inject.Injector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.testng.internal.*;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.invokers.ConfigMethodArguments;
import org.testng.internal.invokers.IInvocationStatus;
import org.testng.internal.invokers.IInvoker;
import org.testng.internal.invokers.InvokedMethod;
import org.testng.internal.objects.ObjectFactoryImpl;
import org.testng.internal.reporters.ParameterSnapshotRecorder;
import org.testng.internal.reporters.ParameterSnapshots;
import org.testng.internal.thread.ThreadUtil;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.reporters.TextReporter;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * Runs the {@code <test>} tags of one suite.
 *
 * <p>The constructor creates a {@link TestRunner} for each {@code <test>}. {@link #run()} runs the
 * {@code @BeforeSuite} methods, then the tests, then the {@code @AfterSuite} methods. TestNG calls
 * the suite listeners before all of that, and again after it.
 */
public class SuiteRunner implements ISuite, ISuiteRunnerListener {

  private static final String DEFAULT_OUTPUT_DIR = "test-output";

  private final Map<String, ISuiteResult> suiteResults = new LinkedHashMap<>();
  private final List<TestRunner> testRunners = new ArrayList<>();
  private final Map<Class<? extends ISuiteListener>, ISuiteListener> listeners =
      new LinkedHashMap<>();

  private @Nullable String outputDir;
  private final XmlSuite xmlSuite;
  private @Nullable Injector parentInjector;

  private final List<ITestListener> testListeners = new ArrayList<>();
  private final Map<Class<? extends IClassListener>, IClassListener> classListeners =
      new LinkedHashMap<>();
  private final @Nullable ITestRunnerFactory tmpRunnerFactory;
  private final DataProviderHolder holder;
  private final ParameterResolverHolder parameterResolverHolder;

  private boolean useDefaultListeners = true;

  // The remote host that ran this suite, or null when the suite ran locally.
  private @Nullable String remoteHost;

  // The configuration of the run.
  // Note: test.multiplelisteners.SimpleReporter#generateReport reads this field by its name. Change
  // that test if you rename the field.
  private final IConfiguration configuration;

  private @Nullable ITestObjectFactory objectFactory;
  private Boolean skipFailedInvocationCounts = Boolean.FALSE;
  private final List<IReporter> reporters = new ArrayList<>();

  private final Map<Class<? extends IInvokedMethodListener>, IInvokedMethodListener>
      invokedMethodListeners;

  private final SuiteRunState suiteState = new SuiteRunState();
  private final IAttributes attributes = new Attributes();
  private final Set<IExecutionVisualiser> visualisers = new HashSet<>();
  private final ITestListener exitCodeListener;

  /**
   * Creates a runner for a suite, with the default listeners off.
   *
   * <p>TestNG still adds a {@code TextReporter} to each {@link TestRunner} that {@code
   * runnerFactory} creates.
   *
   * @param configuration the configuration of the run.
   * @param suite the suite to run.
   * @param outputDir the directory for the reports.
   * @param runnerFactory the factory that creates the {@link TestRunner} of each {@code <test>}.
   * @param comparator the comparator that orders the test methods.
   */
  public SuiteRunner(
      IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      ITestRunnerFactory runnerFactory,
      Comparator<ITestNGMethod> comparator) {
    this(configuration, suite, outputDir, runnerFactory, false, comparator);
  }

  /**
   * Creates a runner for a suite.
   *
   * @param configuration the configuration of the run.
   * @param suite the suite to run.
   * @param outputDir the directory for the reports.
   * @param runnerFactory the factory that creates the {@link TestRunner} of each {@code <test>}, or
   *     {@code null} for the default factory.
   * @param useDefaultListeners whether TestNG adds its default reporters to each {@link
   *     TestRunner}. This applies only when {@code runnerFactory} is {@code null}. With a factory
   *     of your own, TestNG adds a {@code TextReporter} to each {@link TestRunner}, whatever this
   *     value is.
   * @param comparator the comparator that orders the test methods.
   */
  public SuiteRunner(
      IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      @Nullable ITestRunnerFactory runnerFactory,
      boolean useDefaultListeners,
      Comparator<ITestNGMethod> comparator) {
    this(
        configuration,
        suite,
        outputDir,
        runnerFactory,
        useDefaultListeners,
        new ArrayList<>() /* method interceptor */,
        null /* invoked method listeners */,
        new TestListenersContainer() /* test listeners */,
        null /* class listeners */,
        new DataProviderHolder(configuration),
        comparator);
  }

  /**
   * Creates a runner for a suite, with the listeners of the run.
   *
   * @param configuration the configuration of the run.
   * @param suite the suite to run.
   * @param outputDir the directory for the reports.
   * @param runnerFactory the factory that creates the {@link TestRunner} of each {@code <test>}, or
   *     {@code null} for the default factory.
   * @param useDefaultListeners whether TestNG adds its default reporters to each {@link
   *     TestRunner}. This applies only when {@code runnerFactory} is {@code null}. With a factory
   *     of your own, TestNG adds a {@code TextReporter} to each {@link TestRunner}, whatever this
   *     value is.
   * @param methodInterceptors the method interceptors to add to each {@link TestRunner}.
   * @param invokedMethodListener the {@link IInvokedMethodListener} listeners, or {@code null}.
   * @param container the test listeners and the exit code listener.
   * @param classListeners the {@link IClassListener} listeners, or {@code null}.
   * @param holder the data provider listeners and interceptors.
   * @param comparator the comparator that orders the test methods.
   * @throws IllegalArgumentException when {@code comparator} is {@code null}.
   */
  protected SuiteRunner(
      IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      @Nullable ITestRunnerFactory runnerFactory,
      boolean useDefaultListeners,
      List<IMethodInterceptor> methodInterceptors,
      @Nullable Collection<IInvokedMethodListener> invokedMethodListener,
      TestListenersContainer container,
      @Nullable Collection<IClassListener> classListeners,
      DataProviderHolder holder,
      Comparator<ITestNGMethod> comparator) {
    if (comparator == null) {
      throw new IllegalArgumentException("comparator must not be null");
    }
    this.holder = holder;
    this.parameterResolverHolder = new ParameterResolverHolder(configuration);
    this.configuration = configuration;
    this.xmlSuite = suite;
    this.useDefaultListeners = useDefaultListeners;
    this.tmpRunnerFactory = runnerFactory;
    this.exitCodeListener = container.exitCodeListener;
    List<IMethodInterceptor> localMethodInterceptors =
        Optional.ofNullable(methodInterceptors).orElse(new ArrayList<>());
    setOutputDir(outputDir);
    ITestObjectFactory declaredFactory = configuration.getObjectFactory();
    if (declaredFactory == null) {
      declaredFactory = new ObjectFactoryImpl();
      configuration.setObjectFactory(declaredFactory);
    }
    // The anonymous factory uses this variable, so it must be effectively final.
    final ITestObjectFactory configuredFactory = declaredFactory;
    if (suite.getObjectFactoryClass() == null) {
      objectFactory = configuredFactory;
    } else {
      boolean create = !configuredFactory.getClass().equals(suite.getObjectFactoryClass());
      final ITestObjectFactory suiteObjectFactory;
      if (create) {
        suiteObjectFactory =
            Objects.requireNonNull(
                configuredFactory.newInstance(suite.getObjectFactoryClass()),
                "the object factory produced a suite level factory");
      } else {
        suiteObjectFactory = configuredFactory;
      }
      objectFactory =
          new ITestObjectFactory() {
            @Override
            public <T> T newInstance(Class<T> cls, Object... parameters) {
              try {
                return suiteObjectFactory.newInstance(cls, parameters);
              } catch (Exception e) {
                return configuredFactory.newInstance(cls, parameters);
              }
            }

            @Override
            @SuppressWarnings("TypeParameterUnusedInFormals") // signature fixed by the interface
            public <T> T newInstance(String clsName, Object... parameters) {
              try {
                return suiteObjectFactory.newInstance(clsName, parameters);
              } catch (Exception e) {
                return configuredFactory.newInstance(clsName, parameters);
              }
            }

            @Override
            public <T> @Nullable T newInstance(Constructor<T> constructor, Object... parameters) {
              try {
                return suiteObjectFactory.newInstance(constructor, parameters);
              } catch (Exception e) {
                return configuredFactory.newInstance(constructor, parameters);
              }
            }
          };
    }
    // Keep the invoked method listeners that were passed in, one for each class.
    invokedMethodListeners = Collections.synchronizedMap(new LinkedHashMap<>());
    for (IInvokedMethodListener listener :
        Optional.ofNullable(invokedMethodListener).orElse(Collections.emptyList())) {
      invokedMethodListeners.put(listener.getClass(), listener);
    }

    skipFailedInvocationCounts = suite.skipFailedInvocationCounts();
    this.testListeners.addAll(container.listeners);
    for (IClassListener classListener :
        Optional.ofNullable(classListeners).orElse(Collections.emptyList())) {
      this.classListeners.put(classListener.getClass(), classListener);
    }
    // The suite keeps the parameter snapshots of everything it runs, for the reporters. See
    // ParameterSnapshots.
    ParameterSnapshotRecorder parameterSnapshotRecorder =
        new ParameterSnapshotRecorder(ParameterSnapshots.attachTo(this));

    ITestRunnerFactory iTestRunnerFactory = buildRunnerFactory(comparator);

    // Sort the <test> tags in the order in which they appear in the suite file.
    List<XmlTest> xmlTests = xmlSuite.getTests();
    xmlTests.sort(Comparator.comparingInt(XmlTest::getIndex));

    for (XmlTest test : xmlTests) {
      TestRunner tr =
          iTestRunnerFactory.newTestRunner(
              this,
              test,
              invokedMethodListeners.values(),
              new ArrayList<>(this.classListeners.values()),
              this.holder);

      // Use one recorder for the whole suite, and add it to every runner. Add it through the
      // internal methods, not addListener(), because addListener() also adds it to the suite. Add
      // it before the other listeners of the runner, because they read what it records.
      tr.addInternalTestListener(parameterSnapshotRecorder);
      tr.addInternalConfigurationListener(parameterSnapshotRecorder);

      //
      // Add the method interceptors that were passed in
      //
      for (IMethodInterceptor methodInterceptor : localMethodInterceptors) {
        tr.addMethodInterceptor(methodInterceptor);
      }

      testRunners.add(tr);
    }
  }

  @Override
  public XmlSuite getXmlSuite() {
    return xmlSuite;
  }

  @Override
  public String getName() {
    return xmlSuite.getName();
  }

  /**
   * Replaces the object factory of this suite.
   *
   * @param objectFactory the factory that creates objects, such as test class instances.
   */
  public void setObjectFactory(ITestObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  /**
   * Sets whether TestNG adds its default reporters.
   *
   * <p>This method has no effect on the test runners. The constructor already created them, and
   * read the value at that time.
   *
   * @param reportResults whether to add the default reporters.
   */
  public void setReportResults(boolean reportResults) {
    useDefaultListeners = reportResults;
  }

  @Override
  public ITestListener getExitCodeListener() {
    return exitCodeListener;
  }

  private void invokeListeners(boolean start) {
    if (start) {
      for (ISuiteListener sl :
          ListenerOrderDeterminer.order(
              listeners.values(), this.configuration.getListenerComparator())) {
        sl.onStart(this);
      }
    } else {
      List<ISuiteListener> suiteListenersReversed =
          ListenerOrderDeterminer.reversedOrder(
              listeners.values(), this.configuration.getListenerComparator());
      for (ISuiteListener sl : suiteListenersReversed) {
        sl.onFinish(this);
      }
    }
  }

  private void setOutputDir(String dir) {
    String resolved = isStringBlank(dir) && useDefaultListeners ? DEFAULT_OUTPUT_DIR : dir;
    outputDir = null != resolved ? new File(resolved).getAbsolutePath() : null;
  }

  private ITestRunnerFactory buildRunnerFactory(Comparator<ITestNGMethod> comparator) {
    ITestRunnerFactory factory;

    if (null == tmpRunnerFactory) {
      factory =
          new DefaultTestRunnerFactory(
              configuration,
              testListeners.toArray(new ITestListener[0]),
              useDefaultListeners,
              skipFailedInvocationCounts,
              comparator,
              this);
    } else {
      factory =
          new ProxyTestRunnerFactory(
              testListeners.toArray(new ITestListener[0]), tmpRunnerFactory, configuration);
    }

    return factory;
  }

  @Override
  public String getParallel() {
    return xmlSuite.getParallel().toString();
  }

  @Override
  public String getParentModule() {
    return xmlSuite.getParentModule();
  }

  @Override
  public String getGuiceStage() {
    return xmlSuite.getGuiceStage();
  }

  @Override
  public @Nullable Injector getParentInjector() {
    return parentInjector;
  }

  @Override
  public void setParentInjector(Injector injector) {
    parentInjector = injector;
  }

  @Override
  public void run() {
    invokeListeners(true /* start */);
    try {
      privateRun();
    } finally {
      invokeListeners(false /* stop */);
    }
  }

  private void privateRun() {

    // A map keeps each method once, and a linked map keeps the order.
    Map<Method, ITestNGMethod> beforeSuiteMethods = new LinkedHashMap<>();
    Map<Method, ITestNGMethod> afterSuiteMethods = new LinkedHashMap<>();

    IInvoker invoker = null;

    // Take the invoker, and collect the @BeforeSuite and @AfterSuite methods of every test runner.
    for (TestRunner tr : testRunners) {
      // TODO: The invoker should belong to SuiteRunner, not to TestRunner.
      // -- cbeust
      invoker = tr.getInvoker();

      // Add the configuration listeners again. The suite listeners can change them when they
      // run.
      this.configuration.getConfigurationListeners().forEach(tr::addConfigurationListener);

      for (ITestNGMethod m : tr.getBeforeSuiteMethods()) {
        beforeSuiteMethods.put(m.getConstructorOrMethod().getMethod(), m);
      }

      for (ITestNGMethod m : tr.getAfterSuiteMethods()) {
        afterSuiteMethods.put(m.getConstructorOrMethod().getMethod(), m);
      }
    }

    //
    // Run the @BeforeSuite methods. The invoker is null when the suite has no <test> tags of its
    // own, for example when it has only <suite-files>.
    //
    if (invoker != null) {
      if (!beforeSuiteMethods.values().isEmpty()) {
        ConfigMethodArguments arguments =
            new ConfigMethodArguments.Builder()
                .usingConfigMethodsAs(beforeSuiteMethods.values())
                .forSuite(xmlSuite)
                .usingParameters(xmlSuite.getParameters())
                .build();
        invoker.getConfigInvoker().invokeConfigurations(arguments);
      }

      Utils.log("SuiteRunner", 3, "Created " + testRunners.size() + " TestRunners");

      //
      // Run the test runners, in parallel or one after another
      //
      boolean testsInParallel = XmlSuite.ParallelMode.TESTS.equals(xmlSuite.getParallel());
      if (RuntimeBehavior.strictParallelism()) {
        testsInParallel = !XmlSuite.ParallelMode.NONE.equals(xmlSuite.getParallel());
      }
      if (testsInParallel) {
        runInParallelTestMode();
      } else {
        runSequentially();
      }

      //
      // Run the @AfterSuite methods
      //
      if (!afterSuiteMethods.values().isEmpty()) {
        ConfigMethodArguments arguments =
            new ConfigMethodArguments.Builder()
                .usingConfigMethodsAs(afterSuiteMethods.values())
                .forSuite(xmlSuite)
                .usingParameters(xmlSuite.getAllParameters())
                .build();
        invoker.getConfigInvoker().invokeConfigurations(arguments);
      }
    }
  }

  private void addVisualiser(IExecutionVisualiser visualiser) {
    visualisers.add(visualiser);
  }

  private void addReporter(IReporter listener) {
    reporters.add(listener);
  }

  void addConfigurationListener(IConfigurationListener listener) {
    configuration.addConfigurationListener(listener);
  }

  /**
   * Returns the reporters of this suite.
   *
   * @return the list that this suite uses, not a copy.
   */
  public List<IReporter> getReporters() {
    return reporters;
  }

  /**
   * Returns the data provider listeners of this suite.
   *
   * @return the listeners, in the order of the listener comparator when there is one. You cannot
   *     change the returned collection.
   */
  public Collection<IDataProviderListener> getDataProviderListeners() {
    return this.holder.getListeners();
  }

  /**
   * Returns the holder of the {@link IParameterResolver} resolvers of this suite.
   *
   * <p>The test runners share this holder. They do not copy it. So every runner sees a resolver
   * that someone adds later, for example through {@code @Listeners} on a test class. {@link
   * DataProviderHolder} works the same way.
   */
  ParameterResolverHolder getParameterResolverHolder() {
    return this.parameterResolverHolder;
  }

  void addParameterResolvers(Collection<IParameterResolver> resolvers) {
    this.parameterResolverHolder.addResolvers(resolvers);
  }

  private void runSequentially() {
    for (TestRunner tr : testRunners) {
      runTest(tr);
    }
  }

  private final AutoCloseableLock suiteResultsLock = new AutoCloseableLock();

  private void runTest(TestRunner tr) {
    visualisers.forEach(tr::addListener);
    tr.run();

    ISuiteResult sr = new SuiteResult(xmlSuite, tr);
    try (AutoCloseableLock ignore = suiteResultsLock.lock()) {
      suiteResults.putIfAbsent(tr.getName(), sr);
    }
  }

  /**
   * Runs the {@code <test>} tags of this suite in parallel, for {@code parallel="tests"}.
   *
   * <p>This mode works at the suite level, so it needs its own code. {@code
   * TestRunner#createWorkers} handles the other parallel modes. It cannot handle this one, because
   * it sees only one {@code <test>} tag.
   */
  private void runInParallelTestMode() {
    List<Runnable> tasks = new ArrayList<>(testRunners.size());
    for (TestRunner tr : testRunners) {
      tasks.add(new SuiteWorker(tr));
    }

    long timeOut = xmlSuite.getTimeOut(XmlTest.DEFAULT_TIMEOUT_MS);
    boolean waitCompleted =
        ThreadUtil.executeAndWait(
            configuration, "tests", tasks, xmlSuite.getThreadCount(), timeOut);
    // An interrupted wait is not a suite time-out. Do not invent ThreadTimeoutException results.
    if (waitCompleted) {
      recordTimedOutParallelTests(timeOut);
    }
  }

  /**
   * Records a result for each {@code <test>} that the suite time-out stopped.
   *
   * <p>{@code invokeAll} cancels a {@code <test>} worker when the suite time-out fires. A method
   * that stops when interrupted still records its failure a moment later. A method that ignores
   * interruption never returns, so {@link #runTest} never stores a result. The reports and the exit
   * code then omit that {@code <test>}, and the run exits 0.
   *
   * <p>Wait briefly for workers that can still finish. Then fail every unfinished invocation. Then
   * store the {@code <test>} so the reports and the exit code see it.
   */
  private void recordTimedOutParallelTests(long timeOut) {
    if (timeOut == 0) {
      return;
    }
    waitForStragglingParallelTests();
    for (TestRunner tr : testRunners) {
      if (hasSuiteResult(tr)) {
        continue;
      }
      failUnfinishedMethods(tr, timeOut);
      ISuiteResult sr = new SuiteResult(xmlSuite, tr);
      try (AutoCloseableLock ignore = suiteResultsLock.lock()) {
        suiteResults.putIfAbsent(tr.getName(), sr);
      }
    }
  }

  /**
   * Waits up to 250 ms for the cancelled workers to record their results.
   *
   * <p>A cancelled worker that stops when interrupted, records its result shortly after {@code
   * invokeAll} returns. 250 ms is long enough for that. It is also short enough that a worker that
   * ignores the interrupt does not delay the suite for long.
   */
  private void waitForStragglingParallelTests() {
    long deadline = System.currentTimeMillis() + 250;
    while (recordedParallelTestCount() < testRunners.size()
        && System.currentTimeMillis() < deadline) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  private int recordedParallelTestCount() {
    try (AutoCloseableLock ignore = suiteResultsLock.lock()) {
      return suiteResults.size();
    }
  }

  private boolean hasSuiteResult(TestRunner tr) {
    try (AutoCloseableLock ignore = suiteResultsLock.lock()) {
      return suiteResults.containsKey(tr.getName());
    }
  }

  private void failUnfinishedMethods(TestRunner tr, long timeOut) {
    List<ITestResult> created = tr.addTimeoutFailuresForUnfinishedInvocations(timeOut);
    for (ITestResult result : created) {
      TestListenerHelper.runTestListeners(result, tr.getTestListeners());
    }
  }

  private class SuiteWorker implements Runnable {
    private final TestRunner testRunner;

    public SuiteWorker(TestRunner tr) {
      testRunner = tr;
    }

    @Override
    public void run() {
      Utils.log(
          "[SuiteWorker]",
          4,
          "Running XML Test '" + testRunner.getTest().getName() + "' in Parallel");
      runTest(testRunner);
    }
  }

  /**
   * Adds a suite listener, unless this suite already has a listener of the same class.
   *
   * @param reporter the suite listener to add.
   */
  protected void addListener(ISuiteListener reporter) {
    listeners.putIfAbsent(reporter.getClass(), reporter);
  }

  @Override
  public void addListener(ITestNGListener listener) {
    if (listener instanceof IInvokedMethodListener) {
      IInvokedMethodListener invokedMethodListener = (IInvokedMethodListener) listener;
      invokedMethodListeners.put(invokedMethodListener.getClass(), invokedMethodListener);
    }
    if (listener instanceof ISuiteListener) {
      addListener((ISuiteListener) listener);
    }
    if (listener instanceof IExecutionVisualiser) {
      addVisualiser((IExecutionVisualiser) listener);
    }
    if (listener instanceof IReporter) {
      addReporter((IReporter) listener);
    }
    if (listener instanceof IConfigurationListener) {
      addConfigurationListener((IConfigurationListener) listener);
    }
    if (listener instanceof IClassListener) {
      IClassListener classListener = (IClassListener) listener;
      classListeners.putIfAbsent(classListener.getClass(), classListener);
    }
    if (listener instanceof IDataProviderListener) {
      IDataProviderListener listenerObject = (IDataProviderListener) listener;
      this.holder.addListener(listenerObject);
    }
    if (listener instanceof IDataProviderInterceptor) {
      IDataProviderInterceptor interceptor = (IDataProviderInterceptor) listener;
      this.holder.addInterceptor(interceptor);
    }
    if (listener instanceof IParameterResolver) {
      this.parameterResolverHolder.addResolver((IParameterResolver) listener);
    }
    if (listener instanceof ITestListener) {
      for (TestRunner testRunner : testRunners) {
        testRunner.addTestListener((ITestListener) listener);
      }
    }
  }

  @Override
  public String getOutputDirectory() {
    return outputDir + File.separatorChar + getName();
  }

  @Override
  public Map<String, ISuiteResult> getResults() {
    // Return a read-only view, so that callers cannot change the results.
    return Collections.unmodifiableMap(suiteResults);
  }

  /** Returns the value of a parameter of this suite, or {@code null} when there is none. */
  // FIXME: should this method be removed?
  @Override
  public @Nullable String getParameter(String parameterName) {
    return xmlSuite.getParameter(parameterName);
  }

  /** Returns the test methods of every {@code <test>} of this suite, by group name. */
  @Override
  public Map<String, Collection<ITestNGMethod>> getMethodsByGroups() {
    Map<String, Collection<ITestNGMethod>> result = new HashMap<>();

    for (TestRunner tr : testRunners) {
      ITestNGMethod[] methods = tr.getAllTestMethods();
      for (ITestNGMethod m : methods) {
        String[] groups = m.getGroups();
        for (String groupName : groups) {
          Collection<ITestNGMethod> testMethods =
              result.computeIfAbsent(groupName, k -> new ArrayList<>());
          testMethods.add(m);
        }
      }
    }

    return result;
  }

  /** Returns the methods that the {@code <test>} tags of this suite left out. */
  @Override
  public Collection<ITestNGMethod> getExcludedMethods() {
    return testRunners.stream()
        .flatMap(tr -> tr.getExcludedMethods().stream())
        .collect(Collectors.toList());
  }

  @Override
  public @Nullable ITestObjectFactory getObjectFactory() {
    return objectFactory;
  }

  /**
   * Returns the finder that reads the TestNG annotations.
   *
   * @return the annotation finder of the configuration.
   */
  @Override
  public IAnnotationFinder getAnnotationFinder() {
    return configuration.getAnnotationFinder();
  }

  /**
   * The {@link ITestRunnerFactory} that the suite uses when it gets no factory.
   *
   * <p>It adds the default reporters to each {@link TestRunner} when the default listeners are on.
   */
  private static class DefaultTestRunnerFactory implements ITestRunnerFactory {
    private final ITestListener[] failureGenerators;
    private final boolean useDefaultListeners;
    private final boolean skipFailedInvocationCounts;
    private final IConfiguration configuration;
    private final Comparator<ITestNGMethod> comparator;
    private final SuiteRunner suiteRunner;

    public DefaultTestRunnerFactory(
        IConfiguration configuration,
        ITestListener[] failureListeners,
        boolean useDefaultListeners,
        boolean skipFailedInvocationCounts,
        Comparator<ITestNGMethod> comparator,
        SuiteRunner suiteRunner) {
      this.configuration = configuration;
      this.failureGenerators = failureListeners;
      this.useDefaultListeners = useDefaultListeners;
      this.skipFailedInvocationCounts = skipFailedInvocationCounts;
      this.comparator = comparator;
      this.suiteRunner = suiteRunner;
    }

    @Override
    public TestRunner newTestRunner(
        ISuite suite,
        XmlTest test,
        Collection<IInvokedMethodListener> listeners,
        List<IClassListener> classListeners) {
      return newTestRunner(suite, test, listeners, classListeners, Collections.emptyMap());
    }

    @Override
    public TestRunner newTestRunner(
        ISuite suite,
        XmlTest test,
        Collection<IInvokedMethodListener> listeners,
        List<IClassListener> classListeners,
        Map<Class<? extends IDataProviderListener>, IDataProviderListener> dataProviderListeners) {
      DataProviderHolder holder = new DataProviderHolder(this.configuration);
      holder.addListeners(dataProviderListeners.values());
      return newTestRunner(suite, test, listeners, classListeners, holder);
    }

    @Override
    public TestRunner newTestRunner(
        ISuite suite,
        XmlTest test,
        Collection<IInvokedMethodListener> listeners,
        List<IClassListener> classListeners,
        DataProviderHolder holder) {
      boolean skip = skipFailedInvocationCounts;
      if (!skip) {
        skip = test.skipFailedInvocationCounts();
      }
      TestRunner testRunner =
          new TestRunner(
              configuration,
              suite,
              test,
              suite.getOutputDirectory(),
              suite.getAnnotationFinder(),
              skip,
              listeners,
              classListeners,
              comparator,
              holder,
              suiteRunner);

      if (useDefaultListeners) {
        testRunner.addListener(new TestHTMLReporter());
        testRunner.addListener(new JUnitXMLReporter());

        // TODO: TestNG adds these reporters only when the default listeners are on. Maven 2 runs
        // its own reporters, and these would also create directories and files there. Do users
        // need these reporters even with the default listeners off? That is still open.
        testRunner.addListener(new TextReporter(testRunner.getName(), TestRunner.getVerbose()));
      }

      for (ITestListener itl : failureGenerators) {
        testRunner.addTestListener(itl);
      }
      for (IConfigurationListener cl : configuration.getConfigurationListeners()) {
        testRunner.addConfigurationListener(cl);
      }

      return testRunner;
    }
  }

  private static class ProxyTestRunnerFactory implements ITestRunnerFactory {
    private final ITestListener[] failureGenerators;
    private final ITestRunnerFactory target;

    private final IConfiguration configuration;

    public ProxyTestRunnerFactory(
        ITestListener[] failureListeners, ITestRunnerFactory target, IConfiguration configuration) {
      failureGenerators = failureListeners;
      this.target = target;
      this.configuration = configuration;
    }

    @Override
    public TestRunner newTestRunner(
        ISuite suite,
        XmlTest test,
        Collection<IInvokedMethodListener> listeners,
        List<IClassListener> classListeners) {
      return newTestRunner(suite, test, listeners, classListeners, Collections.emptyMap());
    }

    @Override
    public TestRunner newTestRunner(
        ISuite suite,
        XmlTest test,
        Collection<IInvokedMethodListener> listeners,
        List<IClassListener> classListeners,
        Map<Class<? extends IDataProviderListener>, IDataProviderListener> dataProviderListeners) {
      DataProviderHolder holder = new DataProviderHolder(configuration);
      holder.addListeners(dataProviderListeners.values());
      return newTestRunner(suite, test, listeners, classListeners, holder);
    }

    @Override
    public TestRunner newTestRunner(
        ISuite suite,
        XmlTest test,
        Collection<IInvokedMethodListener> listeners,
        List<IClassListener> classListeners,
        DataProviderHolder holder) {
      TestRunner testRunner = target.newTestRunner(suite, test, listeners, classListeners, holder);
      testRunner.addListener(new TextReporter(testRunner.getName(), TestRunner.getVerbose()));

      for (ITestListener itl : failureGenerators) {
        testRunner.addListener(itl);
      }
      return testRunner;
    }
  }

  /**
   * Sets the remote host that runs this suite.
   *
   * @param host the name of the host.
   */
  public void setHost(String host) {
    remoteHost = host;
  }

  @Override
  public @Nullable String getHost() {
    return remoteHost;
  }

  /**
   * Returns the run state of this suite. It records if a {@code @BeforeSuite} or
   * {@code @AfterSuite} method failed.
   */
  @Override
  public SuiteRunState getSuiteState() {
    return suiteState;
  }

  /**
   * Sets whether TestNG skips the remaining invocations of a test method after one invocation
   * fails.
   *
   * <p>This method has no effect on the test runners. The constructor already created them, and
   * read the value at that time. A {@code null} value changes nothing.
   *
   * @param skipFailedInvocationCounts whether to skip the remaining invocations.
   */
  public void setSkipFailedInvocationCounts(Boolean skipFailedInvocationCounts) {
    if (skipFailedInvocationCounts != null) {
      this.skipFailedInvocationCounts = skipFailedInvocationCounts;
    }
  }

  @Override
  public @Nullable Object getAttribute(String name) {
    return attributes.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    attributes.setAttribute(name, value);
  }

  @Override
  public Set<String> getAttributeNames() {
    return attributes.getAttributeNames();
  }

  @Override
  public @Nullable Object removeAttribute(String name) {
    return attributes.removeAttribute(name);
  }

  /////
  // implements ISuiteRunnerListener
  //

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    // Nothing to do after a method runs.
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    if (method == null) {
      throw new NullPointerException("Method should not be null");
    }
    if (method.getTestMethod() instanceof IInvocationStatus) {
      ((IInvocationStatus) method.getTestMethod()).setInvokedAt(method.getDate());
    }
  }

  //
  // implements ISuiteRunnerListener
  /////

  @Override
  public List<IInvokedMethod> getAllInvokedMethods() {
    return testRunners.stream()
        .flatMap(
            tr -> {
              Set<ITestResult> results = new HashSet<>();
              results.addAll(tr.getConfigurationsScheduledForInvocation().getAllResults());
              results.addAll(tr.getPassedConfigurations().getAllResults());
              results.addAll(tr.getFailedConfigurations().getAllResults());
              results.addAll(tr.getSkippedConfigurations().getAllResults());
              results.addAll(tr.getPassedTests().getAllResults());
              results.addAll(tr.getFailedTests().getAllResults());
              results.addAll(tr.getFailedButWithinSuccessPercentageTests().getAllResults());
              results.addAll(tr.getSkippedTests().getAllResults());
              return results.stream();
            })
        .filter(tr -> tr.getMethod() instanceof IInvocationStatus)
        .filter(tr -> ((IInvocationStatus) tr.getMethod()).getInvocationTime() > 0)
        .map(tr -> new InvokedMethod(((IInvocationStatus) tr.getMethod()).getInvocationTime(), tr))
        .collect(Collectors.toList());
  }

  @Override
  public List<ITestNGMethod> getAllMethods() {
    return this.testRunners.stream()
        .flatMap(tr -> Arrays.stream(tr.getAllTestMethods()))
        .collect(Collectors.toList());
  }

  static class TestListenersContainer {
    private final List<ITestListener> listeners = new ArrayList<>();
    private final ITestListener exitCodeListener;

    TestListenersContainer() {
      this(Collections.emptyList(), null);
    }

    TestListenersContainer(
        List<ITestListener> listeners, @Nullable ITestListener exitCodeListener) {
      this.listeners.addAll(listeners);
      this.exitCodeListener =
          Objects.requireNonNullElseGet(exitCodeListener, () -> new ITestListener() {});
    }
  }
}
