package org.testng;

import static org.testng.internal.Utils.isStringBlank;

import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.Attributes;
import org.testng.internal.IConfiguration;
import org.testng.internal.IInvoker;
import org.testng.internal.Systematiser;
import org.testng.internal.Utils;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.thread.ThreadUtil;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.reporters.TextReporter;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.inject.Injector;

/**
 * <CODE>SuiteRunner</CODE> is responsible for running all the tests included in one
 * suite. The test start is triggered by {@link #run()} method.
 */
public class SuiteRunner implements ISuite, IInvokedMethodListener {

  private static final String DEFAULT_OUTPUT_DIR = "test-output";

  private Map<String, ISuiteResult> suiteResults = Collections.synchronizedMap(Maps.<String, ISuiteResult>newLinkedHashMap());
  private List<TestRunner> testRunners = Lists.newArrayList();
  private Map<Class<? extends ISuiteListener>, ISuiteListener> listeners = Maps.newHashMap();
  private TestListenerAdapter textReporter = new TestListenerAdapter();

  private String outputDir;
  private XmlSuite xmlSuite;
  private Injector parentInjector;

  private List<ITestListener> testListeners = Lists.newArrayList();
  private final  Map<Class<? extends IClassListener>, IClassListener> classListeners = Maps.newHashMap();
  private final Map<Class<? extends IDataProviderListener>, IDataProviderListener> dataProviderListeners = Maps.newHashMap();
  private ITestRunnerFactory tmpRunnerFactory;

  private boolean useDefaultListeners = true;

  // The remote host where this suite was run, or null if run locally
  private String remoteHost;

  // The configuration
  private  IConfiguration configuration;

  private ITestObjectFactory objectFactory;
  private Boolean skipFailedInvocationCounts = Boolean.FALSE;
  private List<IReporter> reporters = Lists.newArrayList();

  private Map<Class<? extends IInvokedMethodListener>, IInvokedMethodListener> invokedMethodListeners;

  /** The list of all the methods invoked during this run */
  private final Collection<IInvokedMethod> invokedMethods = new ConcurrentLinkedQueue<>();

  private List<ITestNGMethod> allTestMethods = Lists.newArrayList();
  private SuiteRunState suiteState = new SuiteRunState();
  private IAttributes attributes = new Attributes();

  public SuiteRunner(IConfiguration configuration, XmlSuite suite, String outputDir,
      Comparator<ITestNGMethod> comparator) {
    this(configuration, suite, outputDir, null, comparator);
  }

  public SuiteRunner(IConfiguration configuration, XmlSuite suite, String outputDir,
      ITestRunnerFactory runnerFactory, Comparator<ITestNGMethod> comparator) {
    this(configuration, suite, outputDir, runnerFactory, false, comparator);
  }

  public SuiteRunner(IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      ITestRunnerFactory runnerFactory,
      boolean useDefaultListeners, Comparator<ITestNGMethod> comparator)
  {
    this(configuration, suite, outputDir, runnerFactory, useDefaultListeners,
        new ArrayList<IMethodInterceptor>() /* method interceptor */,
        null /* invoked method listeners */,
        null /* test listeners */,
        null /* class listeners */, Collections.<Class<? extends IDataProviderListener>, IDataProviderListener>emptyMap(), comparator);
  }

  @Deprecated
  public SuiteRunner(IConfiguration configuration, XmlSuite suite, String outputDir) {
    this(configuration, suite, outputDir, (ITestRunnerFactory) null);
  }

  @Deprecated
  public SuiteRunner(IConfiguration configuration, XmlSuite suite, String outputDir, ITestRunnerFactory runnerFactory) {
    this(configuration, suite, outputDir, runnerFactory, false);
  }

  @Deprecated
  public SuiteRunner(IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      ITestRunnerFactory runnerFactory,
      boolean useDefaultListeners)
  {
    this(configuration, suite, outputDir, runnerFactory, useDefaultListeners,
        new ArrayList<IMethodInterceptor>() /* method interceptor */,
        null /* invoked method listeners */,
        null /* test listeners */,
        null /* class listeners */, Collections.<Class<? extends IDataProviderListener>, IDataProviderListener>emptyMap(), Systematiser.getComparator());
  }

  /**
   * @deprecated - This constructor stands deprecated.
   */
  @Deprecated
  //There are no external callers for this constructor but for TestNG. But since this method is a protected method
  //we are following a proper deprecation strategy.
  protected SuiteRunner(IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      ITestRunnerFactory runnerFactory,
      boolean useDefaultListeners,
      List<IMethodInterceptor> methodInterceptors,
      List<IInvokedMethodListener> invokedMethodListeners,
      List<ITestListener> testListeners,
      List<IClassListener> classListeners)
  {
    init(configuration, suite, outputDir, runnerFactory, useDefaultListeners,
        methodInterceptors, invokedMethodListeners, testListeners, classListeners,
        Collections.<Class<? extends IDataProviderListener>, IDataProviderListener>emptyMap(),
        Systematiser.getComparator());
  }

  @Deprecated
  protected SuiteRunner(IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      ITestRunnerFactory runnerFactory,
      boolean useDefaultListeners,
      List<IMethodInterceptor> methodInterceptors,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      Collection<ITestListener> testListeners,
      Collection<IClassListener> classListeners)
  {
    init(configuration, suite, outputDir, runnerFactory, useDefaultListeners,
            methodInterceptors, invokedMethodListeners, testListeners, classListeners,
            Collections.<Class<? extends IDataProviderListener>, IDataProviderListener>emptyMap(),
            Systematiser.getComparator());
  }

  /**
   * @deprecated - This constructor stands deprecated as of TestNG v6.13.
   */
  @Deprecated
  //There are no external callers for this constructor but for TestNG. But since this method is a protected method
  //we are following a proper deprecation strategy.
  protected SuiteRunner(IConfiguration configuration,
      XmlSuite suite,
      String outputDir,
      ITestRunnerFactory runnerFactory,
      boolean useDefaultListeners,
      List<IMethodInterceptor> methodInterceptors,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      Collection<ITestListener> testListeners,
      Collection<IClassListener> classListeners, Comparator<ITestNGMethod> comparator)
  {
    this(configuration, suite, outputDir, runnerFactory, useDefaultListeners,
        methodInterceptors, invokedMethodListeners, testListeners, classListeners,
        Collections.<Class<? extends IDataProviderListener>, IDataProviderListener>emptyMap(), comparator);
  }

  protected SuiteRunner(IConfiguration configuration,
                        XmlSuite suite,
                        String outputDir,
                        ITestRunnerFactory runnerFactory,
                        boolean useDefaultListeners,
                        List<IMethodInterceptor> methodInterceptors,
                        Collection<IInvokedMethodListener> invokedMethodListeners,
                        Collection<ITestListener> testListeners,
                        Collection<IClassListener> classListeners,
                        Map<Class<? extends IDataProviderListener>, IDataProviderListener> dataProviderListeners,
                        Comparator<ITestNGMethod> comparator)
  {
    init(configuration, suite, outputDir, runnerFactory, useDefaultListeners,
            methodInterceptors, invokedMethodListeners, testListeners, classListeners, dataProviderListeners, comparator);
  }

  private void init(IConfiguration configuration,
    XmlSuite suite,
    String outputDir,
    ITestRunnerFactory runnerFactory,
    boolean useDefaultListeners,
    List<IMethodInterceptor> methodInterceptors,
    Collection<IInvokedMethodListener> invokedMethodListener,
    Collection<ITestListener> testListeners,
    Collection<IClassListener> classListeners,
    Map<Class<? extends IDataProviderListener>, IDataProviderListener> dataProviderListeners,
    Comparator<ITestNGMethod> comparator) {
    this.configuration = configuration;
    xmlSuite = suite;
    this.useDefaultListeners = useDefaultListeners;
    tmpRunnerFactory = runnerFactory;
    List<IMethodInterceptor> localMethodInterceptors = methodInterceptors != null ? methodInterceptors : new
        ArrayList<IMethodInterceptor>();
    setOutputDir(outputDir);
    objectFactory = this.configuration.getObjectFactory();
    if(objectFactory == null) {
      objectFactory = suite.getObjectFactory();
    }
    // Add our own IInvokedMethodListener
    invokedMethodListeners = Maps.newHashMap();
    if (invokedMethodListener != null) {
      for (IInvokedMethodListener listener : invokedMethodListener) {
        invokedMethodListeners.put(listener.getClass(), listener);
      }
    }
    invokedMethodListeners.put(getClass(), this);

    skipFailedInvocationCounts = suite.skipFailedInvocationCounts();
    if (null != testListeners) {
      this.testListeners.addAll(testListeners);
    }
    if (null != classListeners) {
      for (IClassListener classListener : classListeners) {
        this.classListeners.put(classListener.getClass(), classListener);
      }
    }
    if (null != dataProviderListeners) {
      this.dataProviderListeners.putAll(dataProviderListeners);
    }
    if (comparator == null) {
      throw new IllegalArgumentException("comparator must not be null");
    }
    ITestRunnerFactory2 iTestRunnerFactory = buildRunnerFactory(comparator);

    // Order the <test> tags based on their order of appearance in testng.xml
    List<XmlTest> xmlTests = xmlSuite.getTests();
    Collections.sort(xmlTests, new Comparator<XmlTest>() {
      @Override
      public int compare(XmlTest arg0, XmlTest arg1) {
        return arg0.getIndex() - arg1.getIndex();
      }
    });

    for (XmlTest test : xmlTests) {
      TestRunner tr = iTestRunnerFactory.newTestRunner(this, test, invokedMethodListeners.values(),
              Lists.newArrayList(this.classListeners.values()), this.dataProviderListeners);

      //
      // Install the method interceptor, if any was passed
      //
      for (IMethodInterceptor methodInterceptor : localMethodInterceptors) {
        tr.addMethodInterceptor(methodInterceptor);
      }

      // Reuse the same text reporter so we can accumulate all the results
      // (this is used to display the final suite report at the end)
      tr.addListener(textReporter);
      testRunners.add(tr);

      // Add the methods found in this test to our global count
      allTestMethods.addAll(Arrays.asList(tr.getAllTestMethods()));
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
    for (ISuiteListener sl : listeners.values()) {
      if (start) {
        sl.onStart(this);
      }
      else {
        sl.onFinish(this);
      }
    }
  }

  private void setOutputDir(String outputdir) {
    if (isStringBlank(outputdir) && useDefaultListeners) {
      outputdir = DEFAULT_OUTPUT_DIR;
    }

    outputDir = (null != outputdir) ? new File(outputdir).getAbsolutePath()
        : null;
  }

  private ITestRunnerFactory2 buildRunnerFactory(Comparator<ITestNGMethod> comparator) {
    ITestRunnerFactory2 factory;

    if (null == tmpRunnerFactory) {
      factory = new DefaultTestRunnerFactory(configuration,
          testListeners.toArray(new ITestListener[testListeners.size()]),
          useDefaultListeners, skipFailedInvocationCounts, comparator);
    }
    else {
      factory = new ProxyTestRunnerFactory(
          testListeners.toArray(new ITestListener[testListeners.size()]),
          tmpRunnerFactory);
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
    }
    finally {
      invokeListeners(false /* stop */);
    }
  }

  private void privateRun() {

    // Map for unicity, Linked for guaranteed order
    Map<Method, ITestNGMethod> beforeSuiteMethods= new LinkedHashMap<>();
    Map<Method, ITestNGMethod> afterSuiteMethods = new LinkedHashMap<>();

    IInvoker invoker = null;

    // Get the invoker and find all the suite level methods
    for (TestRunner tr: testRunners) {
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
      if(! beforeSuiteMethods.values().isEmpty()) {
        invoker.invokeConfigurations(null,
            beforeSuiteMethods.values().toArray(new ITestNGMethod[beforeSuiteMethods.size()]),
            xmlSuite, xmlSuite.getParameters(), null, /* no parameter values */
            null /* instance */
        );
      }

      Utils.log("SuiteRunner", 3, "Created " + testRunners.size() + " TestRunners");

      //
      // Run all the test runners
      //
      boolean testsInParallel = XmlSuite.ParallelMode.TESTS.equals(xmlSuite.getParallel());
      if (!testsInParallel) {
        runSequentially();
      }
      else {
        runInParallelTestMode();
      }

      //
      // Invoke afterSuite methods
      //
      if (! afterSuiteMethods.values().isEmpty()) {
        invoker.invokeConfigurations(null,
              afterSuiteMethods.values().toArray(new ITestNGMethod[afterSuiteMethods.size()]),
            xmlSuite, xmlSuite.getAllParameters(), null, /* no parameter values */

              null /* instance */);
      }
    }
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

  private void runSequentially() {
    for (TestRunner tr : testRunners) {
      runTest(tr);
    }
  }

  private void runTest(TestRunner tr) {
    tr.run();

    ISuiteResult sr = new SuiteResult(xmlSuite, tr);
    suiteResults.put(tr.getName(), sr);
  }

  /**
   * Implement <suite parallel="tests">.
   * Since this kind of parallelism happens at the suite level, we need a special code path
   * to execute it.  All the other parallelism strategies are implemented at the test level
   * in TestRunner#createParallelWorkers (but since this method deals with just one <test>
   * tag, it can't implement <suite parallel="tests">, which is why we're doing it here).
   */
  private void runInParallelTestMode() {
    List<Runnable> tasks= Lists.newArrayList(testRunners.size());
    for(TestRunner tr: testRunners) {
      tasks.add(new SuiteWorker(tr));
    }

    ThreadUtil.execute("tests", tasks, xmlSuite.getThreadCount(),
        xmlSuite.getTimeOut(XmlTest.DEFAULT_TIMEOUT_MS), false);
  }

  private class SuiteWorker implements Runnable {
      private TestRunner testRunner;

      public SuiteWorker(TestRunner tr) {
        testRunner = tr;
      }

      @Override
      public void run() {
        Utils.log("[SuiteWorker]", 4, "Running XML Test '"
                  +  testRunner.getTest().getName() + "' in Parallel");
        runTest(testRunner);
      }
  }

  /**
   * Registers ISuiteListeners interested in reporting the result of the current
   * suite.
   *
   * @param reporter
   */
  protected void addListener(ISuiteListener reporter) {
    if (! listeners.containsKey(reporter.getClass())) {
      listeners.put(reporter.getClass(), reporter);
    }
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
    if (listener instanceof IReporter) {
      addReporter((IReporter) listener);
    }
    if (listener instanceof IConfigurationListener) {
      addConfigurationListener((IConfigurationListener) listener);
    }
    if (listener instanceof IClassListener) {
      IClassListener classListener = (IClassListener) listener;
      if (! classListeners.containsKey(classListener.getClass())) {
        classListeners.put(classListener.getClass(), classListener);
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

  /**
   * @see org.testng.ISuite#getMethodsByGroups()
   */
  @Override
  public Map<String, Collection<ITestNGMethod>> getMethodsByGroups() {
    Map<String, Collection<ITestNGMethod>> result = Maps.newHashMap();

    for (TestRunner tr : testRunners) {
      ITestNGMethod[] methods = tr.getAllTestMethods();
      for (ITestNGMethod m : methods) {
        String[] groups = m.getGroups();
        for (String groupName : groups) {
          Collection<ITestNGMethod> testMethods = result.get(groupName);
          if (null == testMethods) {
            testMethods = Lists.newArrayList();
            result.put(groupName, testMethods);
          }
          testMethods.add(m);
        }
      }
    }

    return result;
  }

  /**
   * @see org.testng.ISuite#getInvokedMethods()
   */
  @Override
  public Collection<ITestNGMethod> getInvokedMethods() {
    return getIncludedOrExcludedMethods(true /* included */);
  }

  /**
   * @see org.testng.ISuite#getExcludedMethods()
   */
  @Override
  public Collection<ITestNGMethod> getExcludedMethods() {
    return getIncludedOrExcludedMethods(false/* included */);
  }

  private Collection<ITestNGMethod> getIncludedOrExcludedMethods(boolean included) {
    List<ITestNGMethod> result= Lists.newArrayList();

    for (TestRunner tr : testRunners) {
      Collection<ITestNGMethod> methods = included ? tr.getInvokedMethods() : tr.getExcludedMethods();
      for (ITestNGMethod m : methods) {
        result.add(m);
      }
    }

    return result;
  }

  @Override
  public IObjectFactory getObjectFactory() {
    return objectFactory instanceof IObjectFactory ? (IObjectFactory) objectFactory : null;
  }

  @Override
  public IObjectFactory2 getObjectFactory2() {
    return objectFactory instanceof IObjectFactory2 ? (IObjectFactory2) objectFactory : null;
  }

  /**
   * Returns the annotation finder for the given annotation type.
   * @return the annotation finder for the given annotation type.
   */
  @Override
  public IAnnotationFinder getAnnotationFinder() {
    return configuration.getAnnotationFinder();
  }

  public static void ppp(String s) {
    System.out.println("[SuiteRunner] " + s);
  }

  /**
   * The default implementation of {@link ITestRunnerFactory}.
   */
  private static class DefaultTestRunnerFactory implements ITestRunnerFactory2 {
    private ITestListener[] failureGenerators;
    private boolean useDefaultListeners;
    private boolean skipFailedInvocationCounts;
    private IConfiguration configuration;
    private final Comparator<ITestNGMethod> comparator;

    public DefaultTestRunnerFactory(IConfiguration configuration,
        ITestListener[] failureListeners,
        boolean useDefaultListeners,
        boolean skipFailedInvocationCounts, Comparator<ITestNGMethod> comparator)
    {
      this.configuration = configuration;
      failureGenerators = failureListeners;
      this.useDefaultListeners = useDefaultListeners;
      this.skipFailedInvocationCounts = skipFailedInvocationCounts;
      this.comparator = comparator;
    }

    @Override
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        Collection<IInvokedMethodListener> listeners, List<IClassListener> classListeners) {
      return newTestRunner(suite, test, listeners, classListeners,
              Collections.<Class<? extends IDataProviderListener>, IDataProviderListener>emptyMap());
    }

    @Override
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        Collection<IInvokedMethodListener> listeners, List<IClassListener> classListeners,
        Map<Class<? extends IDataProviderListener>, IDataProviderListener>  dataProviderListeners) {
      boolean skip = skipFailedInvocationCounts;
      if (! skip) {
        skip = test.skipFailedInvocationCounts();
      }
      TestRunner testRunner = new TestRunner(configuration, suite, test,
              suite.getOutputDirectory(), suite.getAnnotationFinder(), skip,
              listeners, classListeners, comparator, dataProviderListeners);

      if (useDefaultListeners) {
        testRunner.addListener(new TestHTMLReporter());
        testRunner.addListener(new JUnitXMLReporter());

        //TODO: Moved these here because maven2 has output reporters running
        //already, the output from these causes directories to be created with
        //files. This is not the desired behaviour of running tests in maven2.
        //Don't know what to do about this though, are people relying on these
        //to be added even with defaultListeners set to false?
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

  private static class ProxyTestRunnerFactory implements ITestRunnerFactory2 {
    private ITestListener[] failureGenerators;
    private ITestRunnerFactory target;

    public ProxyTestRunnerFactory(ITestListener[] failureListeners, ITestRunnerFactory target) {
      failureGenerators = failureListeners;
      this.target = target;
    }

    @Override
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        Collection<IInvokedMethodListener> listeners, List<IClassListener> classListeners) {
      return newTestRunner(suite, test, listeners, classListeners,
              Collections.<Class<? extends IDataProviderListener>, IDataProviderListener>emptyMap());
    }

    @Override
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
                                    Collection<IInvokedMethodListener> listeners, List<IClassListener> classListeners,
                                    Map<Class<? extends IDataProviderListener>,IDataProviderListener> dataProviderListeners) {
      TestRunner testRunner;
      if (target instanceof ITestRunnerFactory2) {
        testRunner = ((ITestRunnerFactory2)target).newTestRunner(suite, test, listeners, classListeners, dataProviderListeners);
      } else {
        testRunner = target.newTestRunner(suite, test, listeners, classListeners);
      }

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



  /**
   * @see org.testng.ISuite#getSuiteState()
   */
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
    //Empty implementation.
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    if (method == null) {
      throw new NullPointerException("Method should not be null");
    }
    invokedMethods.add(method);
  }

  //
  // implements IInvokedMethodListener
  /////

  @Override
  public List<IInvokedMethod> getAllInvokedMethods() {
    return new ArrayList<>(invokedMethods);
  }

  @Override
  public List<ITestNGMethod> getAllMethods() {
    return allTestMethods;
  }
}
