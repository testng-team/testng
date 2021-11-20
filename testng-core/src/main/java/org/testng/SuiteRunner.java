package org.testng;

import static org.testng.internal.Utils.isStringBlank;

import com.google.inject.Injector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.*;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.invokers.ConfigMethodArguments;
import org.testng.internal.invokers.ConfigMethodArguments.Builder;
import org.testng.internal.invokers.IInvocationStatus;
import org.testng.internal.invokers.IInvoker;
import org.testng.internal.invokers.InvokedMethod;
import org.testng.internal.thread.ThreadUtil;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.reporters.TextReporter;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * <CODE>SuiteRunner</CODE> is responsible for running all the tests included in one suite. The test
 * start is triggered by {@link #run()} method.
 */
public class SuiteRunner implements ISuite, IInvokedMethodListener {

  private static final String DEFAULT_OUTPUT_DIR = "test-output";

  private final Map<String, ISuiteResult> suiteResults =
      Collections.synchronizedMap(Maps.newLinkedHashMap());
  private final List<TestRunner> testRunners = Lists.newArrayList();
  private final Map<Class<? extends ISuiteListener>, ISuiteListener> listeners =
      Maps.newLinkedHashMap();

  private String outputDir;
  private XmlSuite xmlSuite;
  private Injector parentInjector;

  private final List<ITestListener> testListeners = Lists.newArrayList();
  private final Map<Class<? extends IClassListener>, IClassListener> classListeners =
      Maps.newLinkedHashMap();
  private ITestRunnerFactory tmpRunnerFactory;
  private final DataProviderHolder holder = new DataProviderHolder();

  private boolean useDefaultListeners = true;

  // The remote host where this suite was run, or null if run locally
  private String remoteHost;

  // The configuration
  // Note: adjust test.multiplelisteners.SimpleReporter#generateReport test if renaming the field
  private IConfiguration configuration;

  private ITestObjectFactory objectFactory;
  private Boolean skipFailedInvocationCounts = Boolean.FALSE;
  private final List<IReporter> reporters = Lists.newArrayList();

  private Map<Class<? extends IInvokedMethodListener>, IInvokedMethodListener>
      invokedMethodListeners;

  private final SuiteRunState suiteState = new SuiteRunState();
  private final IAttributes attributes = new Attributes();
  private final Set<IExecutionVisualiser> visualisers = Sets.newHashSet();

  public SuiteRunner(
      IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      ITestRunnerFactory runnerFactory,
      Comparator<ITestNGMethod> comparator) {
    this(configuration, suite, outputDir, runnerFactory, false, comparator);
  }

  public SuiteRunner(
      IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      ITestRunnerFactory runnerFactory,
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
        null /* test listeners */,
        null /* class listeners */,
        new DataProviderHolder(),
        comparator);
  }

  protected SuiteRunner(
      IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      ITestRunnerFactory runnerFactory,
      boolean useDefaultListeners,
      List<IMethodInterceptor> methodInterceptors,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      Collection<ITestListener> testListeners,
      Collection<IClassListener> classListeners,
      DataProviderHolder holder,
      Comparator<ITestNGMethod> comparator) {
    init(
        configuration,
        suite,
        outputDir,
        runnerFactory,
        useDefaultListeners,
        methodInterceptors,
        invokedMethodListeners,
        testListeners,
        classListeners,
        holder,
        comparator);
  }

  private void init(
      IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      ITestRunnerFactory runnerFactory,
      boolean useDefaultListeners,
      List<IMethodInterceptor> methodInterceptors,
      Collection<IInvokedMethodListener> invokedMethodListener,
      Collection<ITestListener> testListeners,
      Collection<IClassListener> classListeners,
      DataProviderHolder attribs,
      Comparator<ITestNGMethod> comparator) {
    if (comparator == null) {
      throw new IllegalArgumentException("comparator must not be null");
    }
    this.holder.merge(attribs);
    this.configuration = configuration;
    this.xmlSuite = suite;
    this.useDefaultListeners = useDefaultListeners;
    this.tmpRunnerFactory = runnerFactory;
    List<IMethodInterceptor> localMethodInterceptors =
        Optional.ofNullable(methodInterceptors).orElse(Lists.newArrayList());
    setOutputDir(outputDir);
    if (suite.getObjectFactoryClass() == null) {
      objectFactory = configuration.getObjectFactory();
    } else {
      boolean create =
          !configuration.getObjectFactory().getClass().equals(suite.getObjectFactoryClass());
      final ITestObjectFactory suiteObjectFactory;
      if (create) {
        if (objectFactory == null) {
          objectFactory = configuration.getObjectFactory();
        }
        // Dont keep creating the object factory repeatedly since our current object factory
        // Was already created based off of a suite level object factory.
        suiteObjectFactory = objectFactory.newInstance(suite.getObjectFactoryClass());
      } else {
        suiteObjectFactory = configuration.getObjectFactory();
      }
      objectFactory =
          new ITestObjectFactory() {
            @Override
            public <T> T newInstance(Class<T> cls, Object... parameters) {
              try {
                return suiteObjectFactory.newInstance(cls, parameters);
              } catch (Exception e) {
                return configuration.getObjectFactory().newInstance(cls, parameters);
              }
            }

            @Override
            public <T> T newInstance(String clsName, Object... parameters) {
              try {
                return suiteObjectFactory.newInstance(clsName, parameters);
              } catch (Exception e) {
                return configuration.getObjectFactory().newInstance(clsName, parameters);
              }
            }

            @Override
            public <T> T newInstance(Constructor<T> constructor, Object... parameters) {
              try {
                return suiteObjectFactory.newInstance(constructor, parameters);
              } catch (Exception e) {
                return configuration.getObjectFactory().newInstance(constructor, parameters);
              }
            }
          };
    }
    // Add our own IInvokedMethodListener
    invokedMethodListeners = Maps.synchronizedLinkedHashMap();
    for (IInvokedMethodListener listener :
        Optional.ofNullable(invokedMethodListener).orElse(Collections.emptyList())) {
      invokedMethodListeners.put(listener.getClass(), listener);
    }
    invokedMethodListeners.put(getClass(), this);

    skipFailedInvocationCounts = suite.skipFailedInvocationCounts();
    if (null != testListeners) {
      this.testListeners.addAll(testListeners);
    }
    for (IClassListener classListener :
        Optional.ofNullable(classListeners).orElse(Collections.emptyList())) {
      this.classListeners.put(classListener.getClass(), classListener);
    }
    ITestRunnerFactory iTestRunnerFactory = buildRunnerFactory(comparator);

    // Order the <test> tags based on their order of appearance in testng.xml
    List<XmlTest> xmlTests = xmlSuite.getTests();
    xmlTests.sort(Comparator.comparingInt(XmlTest::getIndex));

    for (XmlTest test : xmlTests) {
      TestRunner tr =
          iTestRunnerFactory.newTestRunner(
              this,
              test,
              invokedMethodListeners.values(),
              Lists.newArrayList(this.classListeners.values()),
              this.holder);

      //
      // Install the method interceptor, if any was passed
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

  public void setObjectFactory(ITestObjectFactory objectFactory) {
    this.objectFactory = objectFactory;
  }

  public void setReportResults(boolean reportResults) {
    useDefaultListeners = reportResults;
  }

  private void invokeListeners(boolean start) {
    if (start) {
      for (ISuiteListener sl : Lists.newArrayList(listeners.values())) {
        sl.onStart(this);
      }
    } else {
      List<ISuiteListener> suiteListenersReversed = Lists.newReversedArrayList(listeners.values());
      for (ISuiteListener sl : suiteListenersReversed) {
        sl.onFinish(this);
      }
    }
  }

  private void setOutputDir(String outputdir) {
    if (isStringBlank(outputdir) && useDefaultListeners) {
      outputdir = DEFAULT_OUTPUT_DIR;
    }

    outputDir = (null != outputdir) ? new File(outputdir).getAbsolutePath() : null;
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
              comparator);
    } else {
      factory =
          new ProxyTestRunnerFactory(testListeners.toArray(new ITestListener[0]), tmpRunnerFactory);
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
  public Injector getParentInjector() {
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

    // Map for unicity, Linked for guaranteed order
    Map<Method, ITestNGMethod> beforeSuiteMethods = new LinkedHashMap<>();
    Map<Method, ITestNGMethod> afterSuiteMethods = new LinkedHashMap<>();

    IInvoker invoker = null;

    // Get the invoker and find all the suite level methods
    for (TestRunner tr : testRunners) {
      // TODO: Code smell.  Invoker should belong to SuiteRunner, not TestRunner
      // -- cbeust
      invoker = tr.getInvoker();

      for (ITestNGMethod m : tr.getBeforeSuiteMethods()) {
        beforeSuiteMethods.put(m.getConstructorOrMethod().getMethod(), m);
      }

      for (ITestNGMethod m : tr.getAfterSuiteMethods()) {
        afterSuiteMethods.put(m.getConstructorOrMethod().getMethod(), m);
      }
    }

    //
    // Invoke beforeSuite methods (the invoker can be null
    // if the suite we are currently running only contains
    // a <file-suite> tag and no real tests)
    //
    if (invoker != null) {
      if (!beforeSuiteMethods.values().isEmpty()) {
        ConfigMethodArguments arguments =
            new Builder()
                .usingConfigMethodsAs(beforeSuiteMethods.values())
                .forSuite(xmlSuite)
                .usingParameters(xmlSuite.getParameters())
                .build();
        invoker.getConfigInvoker().invokeConfigurations(arguments);
      }

      Utils.log("SuiteRunner", 3, "Created " + testRunners.size() + " TestRunners");

      //
      // Run all the test runners
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
      // Invoke afterSuite methods
      //
      if (!afterSuiteMethods.values().isEmpty()) {
        ConfigMethodArguments arguments =
            new Builder()
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

  public List<IReporter> getReporters() {
    return reporters;
  }

  public Collection<IDataProviderListener> getDataProviderListeners() {
    return this.holder.getListeners();
  }

  private void runSequentially() {
    for (TestRunner tr : testRunners) {
      runTest(tr);
    }
  }

  private void runTest(TestRunner tr) {
    visualisers.forEach(tr::addListener);
    tr.run();

    ISuiteResult sr = new SuiteResult(xmlSuite, tr);
    synchronized (suiteResults) {
      suiteResults.put(tr.getName(), sr);
    }
  }

  /**
   * Implement <suite parallel="tests">. Since this kind of parallelism happens at the suite level,
   * we need a special code path to execute it. All the other parallelism strategies are implemented
   * at the test level in TestRunner#createParallelWorkers (but since this method deals with just
   * one &lt;test&gt; tag, it can't implement <suite parallel="tests">, which is why we're doing it
   * here).
   */
  private void runInParallelTestMode() {
    List<Runnable> tasks = Lists.newArrayList(testRunners.size());
    for (TestRunner tr : testRunners) {
      tasks.add(new SuiteWorker(tr));
    }

    ThreadUtil.execute(
        "tests", tasks, xmlSuite.getThreadCount(), xmlSuite.getTimeOut(XmlTest.DEFAULT_TIMEOUT_MS));
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

  /** @param reporter The ISuiteListener interested in reporting the result of the current suite. */
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
    return suiteResults;
  }

  /**
   * FIXME: should be removed?
   *
   * @see org.testng.ISuite#getParameter(java.lang.String)
   */
  @Override
  public String getParameter(String parameterName) {
    return xmlSuite.getParameter(parameterName);
  }

  /** @see org.testng.ISuite#getMethodsByGroups() */
  @Override
  public Map<String, Collection<ITestNGMethod>> getMethodsByGroups() {
    Map<String, Collection<ITestNGMethod>> result = Maps.newHashMap();

    for (TestRunner tr : testRunners) {
      ITestNGMethod[] methods = tr.getAllTestMethods();
      for (ITestNGMethod m : methods) {
        String[] groups = m.getGroups();
        for (String groupName : groups) {
          Collection<ITestNGMethod> testMethods =
              result.computeIfAbsent(groupName, k -> Lists.newArrayList());
          testMethods.add(m);
        }
      }
    }

    return result;
  }

  /** @see org.testng.ISuite#getExcludedMethods() */
  @Override
  public Collection<ITestNGMethod> getExcludedMethods() {
    return testRunners.stream()
        .flatMap(tr -> tr.getExcludedMethods().stream())
        .collect(Collectors.toList());
  }

  @Override
  public ITestObjectFactory getObjectFactory() {
    return objectFactory;
  }

  /**
   * Returns the annotation finder for the given annotation type.
   *
   * @return the annotation finder for the given annotation type.
   */
  @Override
  public IAnnotationFinder getAnnotationFinder() {
    return configuration.getAnnotationFinder();
  }

  /** The default implementation of {@link ITestRunnerFactory}. */
  private static class DefaultTestRunnerFactory implements ITestRunnerFactory {
    private final ITestListener[] failureGenerators;
    private final boolean useDefaultListeners;
    private final boolean skipFailedInvocationCounts;
    private final IConfiguration configuration;
    private final Comparator<ITestNGMethod> comparator;

    public DefaultTestRunnerFactory(
        IConfiguration configuration,
        ITestListener[] failureListeners,
        boolean useDefaultListeners,
        boolean skipFailedInvocationCounts,
        Comparator<ITestNGMethod> comparator) {
      this.configuration = configuration;
      this.failureGenerators = failureListeners;
      this.useDefaultListeners = useDefaultListeners;
      this.skipFailedInvocationCounts = skipFailedInvocationCounts;
      this.comparator = comparator;
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
      DataProviderHolder holder = new DataProviderHolder();
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
              holder);

      if (useDefaultListeners) {
        testRunner.addListener(new TestHTMLReporter());
        testRunner.addListener(new JUnitXMLReporter());

        // TODO: Moved these here because maven2 has output reporters running
        // already, the output from these causes directories to be created with
        // files. This is not the desired behaviour of running tests in maven2.
        // Don't know what to do about this though, are people relying on these
        // to be added even with defaultListeners set to false?
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

    public ProxyTestRunnerFactory(ITestListener[] failureListeners, ITestRunnerFactory target) {
      failureGenerators = failureListeners;
      this.target = target;
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
      DataProviderHolder holder = new DataProviderHolder();
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

  public void setHost(String host) {
    remoteHost = host;
  }

  @Override
  public String getHost() {
    return remoteHost;
  }

  /** @see org.testng.ISuite#getSuiteState() */
  @Override
  public SuiteRunState getSuiteState() {
    return suiteState;
  }

  public void setSkipFailedInvocationCounts(Boolean skipFailedInvocationCounts) {
    if (skipFailedInvocationCounts != null) {
      this.skipFailedInvocationCounts = skipFailedInvocationCounts;
    }
  }

  @Override
  public Object getAttribute(String name) {
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
  public Object removeAttribute(String name) {
    return attributes.removeAttribute(name);
  }

  /////
  // implements IInvokedMethodListener
  //

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
    // Empty implementation.
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
  // implements IInvokedMethodListener
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
}
