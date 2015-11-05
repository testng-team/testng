package org.testng;

import static org.testng.internal.Utils.isStringBlank;

import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.Attributes;
import org.testng.internal.IConfiguration;
import org.testng.internal.IInvoker;
import org.testng.internal.Utils;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.thread.ThreadUtil;
import org.testng.reporters.JUnitXMLReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.reporters.TextReporter;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.io.Serializable;
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
import com.google.inject.Injector;

/**
 * <CODE>SuiteRunner</CODE> is responsible for running all the tests included in one
 * suite. The test start is triggered by {@link #run()} method.
 *
 * @author Cedric Beust, Apr 26, 2004
 */
public class SuiteRunner implements ISuite, Serializable, IInvokedMethodListener {

  /* generated */
  private static final long serialVersionUID = 5284208932089503131L;

  private static final String DEFAULT_OUTPUT_DIR = "test-output";

  private Map<String, ISuiteResult> m_suiteResults = Collections.synchronizedMap(Maps.<String, ISuiteResult>newLinkedHashMap());
  transient private List<TestRunner> m_testRunners = Lists.newArrayList();
  transient private Map<Class<? extends ISuiteListener>, ISuiteListener> m_listeners = Maps.newHashMap();
  transient private TestListenerAdapter m_textReporter = new TestListenerAdapter();

  private String m_outputDir; // DEFAULT_OUTPUT_DIR;
  private XmlSuite m_suite;
  private Injector m_parentInjector;

  transient private List<ITestListener> m_testListeners = Lists.newArrayList();
  transient private List<IClassListener> m_classListeners = Lists.newArrayList();
  transient private ITestRunnerFactory m_tmpRunnerFactory;

  transient private ITestRunnerFactory m_runnerFactory;
  transient private boolean m_useDefaultListeners = true;

  // The remote host where this suite was run, or null if run locally
  private String m_host;

  // The configuration
  transient private IConfiguration m_configuration;

  transient private ITestObjectFactory m_objectFactory;
  transient private Boolean m_skipFailedInvocationCounts = Boolean.FALSE;

  transient private List<IMethodInterceptor> m_methodInterceptors;
  private Map<Class<? extends IInvokedMethodListener>, IInvokedMethodListener> m_invokedMethodListeners;

  /** The list of all the methods invoked during this run */
  private List<IInvokedMethod> m_invokedMethods =
      Collections.synchronizedList(Lists.<IInvokedMethod>newArrayList());

  private List<ITestNGMethod> m_allTestMethods = Lists.newArrayList();

//  transient private IAnnotationTransformer m_annotationTransformer = null;

  public SuiteRunner(IConfiguration configuration, XmlSuite suite,
      String outputDir)
  {
    this(configuration, suite, outputDir, null);
  }

  public SuiteRunner(IConfiguration configuration, XmlSuite suite, String outputDir,
      ITestRunnerFactory runnerFactory)
  {
    this(configuration, suite, outputDir, runnerFactory, false);
  }

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
        null /* class listeners */);
  }

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
    init(configuration, suite, outputDir, runnerFactory, useDefaultListeners, methodInterceptors, invokedMethodListeners, testListeners, classListeners);
  }

  private void init(IConfiguration configuration,
    XmlSuite suite,
    String outputDir,
    ITestRunnerFactory runnerFactory,
    boolean useDefaultListeners,
    List<IMethodInterceptor> methodInterceptors,
    List<IInvokedMethodListener> invokedMethodListener,
    List<ITestListener> testListeners,
    List<IClassListener> classListeners)
  {
    m_configuration = configuration;
    m_suite = suite;
    m_useDefaultListeners = useDefaultListeners;
    m_tmpRunnerFactory= runnerFactory;
    m_methodInterceptors = methodInterceptors != null ? methodInterceptors : new ArrayList<IMethodInterceptor>();
    setOutputDir(outputDir);
    m_objectFactory = m_configuration.getObjectFactory();
    if(m_objectFactory == null) {
      m_objectFactory = suite.getObjectFactory();
    }
    // Add our own IInvokedMethodListener
    m_invokedMethodListeners = Maps.newHashMap();
    if (invokedMethodListener != null) {
      for (IInvokedMethodListener listener : invokedMethodListener) {
        m_invokedMethodListeners.put(listener.getClass(), listener);
      }
    }
    m_invokedMethodListeners.put(getClass(), this);

    m_skipFailedInvocationCounts = suite.skipFailedInvocationCounts();
    if (null != testListeners) {
      m_testListeners.addAll(testListeners);
    }
    if (null != classListeners) {
      m_classListeners.addAll(classListeners);
    }
    m_runnerFactory = buildRunnerFactory();

    // Order the <test> tags based on their order of appearance in testng.xml
    List<XmlTest> xmlTests = m_suite.getTests();
    Collections.sort(xmlTests, new Comparator<XmlTest>() {
      @Override
      public int compare(XmlTest arg0, XmlTest arg1) {
        return arg0.getIndex() - arg1.getIndex();
      }
    });

    for (XmlTest test : xmlTests) {
      TestRunner tr = m_runnerFactory.newTestRunner(this, test, m_invokedMethodListeners.values(), m_classListeners);

      //
      // Install the method interceptor, if any was passed
      //
      for (IMethodInterceptor methodInterceptor : m_methodInterceptors) {
        tr.addMethodInterceptor(methodInterceptor);
      }

      // Reuse the same text reporter so we can accumulate all the results
      // (this is used to display the final suite report at the end)
      tr.addListener(m_textReporter);
      m_testRunners.add(tr);

      // Add the methods found in this test to our global count
      m_allTestMethods.addAll(Arrays.asList(tr.getAllTestMethods()));
    }
  }

  @Override
  public XmlSuite getXmlSuite() {
    return m_suite;
  }

  @Override
  public String getName() {
    return m_suite.getName();
  }

  public void setObjectFactory(ITestObjectFactory objectFactory) {
    m_objectFactory = objectFactory;
  }

  public void setReportResults(boolean reportResults) {
    m_useDefaultListeners = reportResults;
  }

  private void invokeListeners(boolean start) {
    for (ISuiteListener sl : m_listeners.values()) {
      if (start) {
        sl.onStart(this);
      }
      else {
        sl.onFinish(this);
      }
    }
  }

  private void setOutputDir(String outputdir) {
    if (isStringBlank(outputdir) && m_useDefaultListeners) {
      outputdir = DEFAULT_OUTPUT_DIR;
    }

    m_outputDir = (null != outputdir) ? new File(outputdir).getAbsolutePath()
        : null;
  }

  private ITestRunnerFactory buildRunnerFactory() {
    ITestRunnerFactory factory = null;

    if (null == m_tmpRunnerFactory) {
      factory = new DefaultTestRunnerFactory(m_configuration,
          m_testListeners.toArray(new ITestListener[m_testListeners.size()]),
          m_useDefaultListeners, m_skipFailedInvocationCounts);
    }
    else {
      factory = new ProxyTestRunnerFactory(
          m_testListeners.toArray(new ITestListener[m_testListeners.size()]),
          m_tmpRunnerFactory);
    }

    return factory;
  }

  @Override
  public String getParallel() {
    return m_suite.getParallel().toString();
  }

  public String getParentModule() {
    return m_suite.getParentModule();
  }

  @Override
  public String getGuiceStage() {
    return m_suite.getGuiceStage();
  }

  public Injector getParentInjector() {
    return m_parentInjector;
  }

  public void setParentInjector(Injector injector) {
    m_parentInjector = injector;
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
    for (TestRunner tr: m_testRunners) {
      // TODO: Code smell.  Invoker should belong to SuiteRunner, not TestRunner
      // -- cbeust
      invoker = tr.getInvoker();

      for (ITestNGMethod m : tr.getBeforeSuiteMethods()) {
        beforeSuiteMethods.put(m.getMethod(), m);
      }

      for (ITestNGMethod m : tr.getAfterSuiteMethods()) {
        afterSuiteMethods.put(m.getMethod(), m);
      }
    }

    //
    // Invoke beforeSuite methods (the invoker can be null
    // if the suite we are currently running only contains
    // a <file-suite> tag and no real tests)
    //
    if (invoker != null) {
      if(beforeSuiteMethods.values().size() > 0) {
        invoker.invokeConfigurations(null,
            beforeSuiteMethods.values().toArray(new ITestNGMethod[beforeSuiteMethods.size()]),
            m_suite, m_suite.getParameters(), null, /* no parameter values */
            null /* instance */
        );
      }

      Utils.log("SuiteRunner", 3, "Created " + m_testRunners.size() + " TestRunners");

      //
      // Run all the test runners
      //
      boolean testsInParallel = XmlSuite.ParallelMode.TESTS.equals(m_suite.getParallel());
      if (!testsInParallel) {
        runSequentially();
      }
      else {
        runInParallelTestMode();
      }

//      SuitePlan sp = new SuitePlan();
//      for (TestRunner tr : m_testRunners) {
//        sp.addTestPlan(tr.getTestPlan());
//      }

//      sp.dump();

      //
      // Invoke afterSuite methods
      //
      if (afterSuiteMethods.values().size() > 0) {
        invoker.invokeConfigurations(null,
              afterSuiteMethods.values().toArray(new ITestNGMethod[afterSuiteMethods.size()]),
              m_suite, m_suite.getAllParameters(), null, /* no parameter values */

              null /* instance */);
      }
    }
  }

  private List<IReporter> m_reporters = Lists.newArrayList();

  private void addReporter(IReporter listener) {
    m_reporters.add(listener);
  }

  void addConfigurationListener(IConfigurationListener listener) {
    m_configuration.addConfigurationListener(listener);
  }

  public List<IReporter> getReporters() {
    return m_reporters;
  }

  private void runSequentially() {
    for (TestRunner tr : m_testRunners) {
      runTest(tr);
    }
  }

  private void runTest(TestRunner tr) {
    tr.run();

    ISuiteResult sr = new SuiteResult(m_suite, tr);
    m_suiteResults.put(tr.getName(), sr);
  }

  /**
   * Implement <suite parallel="tests">.
   * Since this kind of parallelism happens at the suite level, we need a special code path
   * to execute it.  All the other parallelism strategies are implemented at the test level
   * in TestRunner#createParallelWorkers (but since this method deals with just one <test>
   * tag, it can't implement <suite parallel="tests">, which is why we're doing it here).
   */
  private void runInParallelTestMode() {
    List<Runnable> tasks= Lists.newArrayList(m_testRunners.size());
    for(TestRunner tr: m_testRunners) {
      tasks.add(new SuiteWorker(tr));
    }

    ThreadUtil.execute(tasks, m_suite.getThreadCount(),
        m_suite.getTimeOut(XmlTest.DEFAULT_TIMEOUT_MS), false);
  }

  private class SuiteWorker implements Runnable {
      private TestRunner m_testRunner;

      public SuiteWorker(TestRunner tr) {
        m_testRunner = tr;
      }

      @Override
      public void run() {
        Utils.log("[SuiteWorker]", 4, "Running XML Test '"
                  +  m_testRunner.getTest().getName() + "' in Parallel");
        runTest(m_testRunner);
      }
  }

  /**
   * Registers ISuiteListeners interested in reporting the result of the current
   * suite.
   *
   * @param reporter
   */
  protected void addListener(ISuiteListener reporter) {
    m_listeners.put(reporter.getClass(), reporter);
  }

  @Override
  public void addListener(ITestNGListener listener) {
    if (listener instanceof IInvokedMethodListener) {
      IInvokedMethodListener invokedMethodListener = (IInvokedMethodListener) listener;
      m_invokedMethodListeners.put(invokedMethodListener.getClass(), invokedMethodListener);
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
  }

  @Override
  public String getOutputDirectory() {
    return m_outputDir + File.separatorChar + getName();
  }

  @Override
  public Map<String, ISuiteResult> getResults() {
    return m_suiteResults;
  }

  /**
   * FIXME: should be removed?
   *
   * @see org.testng.ISuite#getParameter(java.lang.String)
   */
  @Override
  public String getParameter(String parameterName) {
    return m_suite.getParameter(parameterName);
  }

  /**
   * @see org.testng.ISuite#getMethodsByGroups()
   */
  @Override
  public Map<String, Collection<ITestNGMethod>> getMethodsByGroups() {
    Map<String, Collection<ITestNGMethod>> result = Maps.newHashMap();

    for (TestRunner tr : m_testRunners) {
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

    for (TestRunner tr : m_testRunners) {
      Collection<ITestNGMethod> methods = included ? tr.getInvokedMethods() : tr.getExcludedMethods();
      for (ITestNGMethod m : methods) {
        result.add(m);
      }
    }

    return result;
  }

  @Override
  public IObjectFactory getObjectFactory() {
    return m_objectFactory instanceof IObjectFactory ? (IObjectFactory) m_objectFactory : null;
  }

  @Override
  public IObjectFactory2 getObjectFactory2() {
    return m_objectFactory instanceof IObjectFactory2 ? (IObjectFactory2) m_objectFactory : null;
  }

  /**
   * Returns the annotation finder for the given annotation type.
   * @return the annotation finder for the given annotation type.
   */
  @Override
  public IAnnotationFinder getAnnotationFinder() {
    return m_configuration.getAnnotationFinder();
  }

  public static void ppp(String s) {
    System.out.println("[SuiteRunner] " + s);
  }

  /**
   * The default implementation of {@link ITestRunnerFactory}.
   */
  private static class DefaultTestRunnerFactory implements ITestRunnerFactory {
    private ITestListener[] m_failureGenerators;
    private boolean m_useDefaultListeners;
    private boolean m_skipFailedInvocationCounts;
    private IConfiguration m_configuration;

    public DefaultTestRunnerFactory(IConfiguration configuration,
        ITestListener[] failureListeners,
        boolean useDefaultListeners,
        boolean skipFailedInvocationCounts)
    {
      m_configuration = configuration;
      m_failureGenerators = failureListeners;
      m_useDefaultListeners = useDefaultListeners;
      m_skipFailedInvocationCounts = skipFailedInvocationCounts;
    }

    @Override
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        Collection<IInvokedMethodListener> listeners, List<IClassListener> classListeners) {
      boolean skip = m_skipFailedInvocationCounts;
      if (! skip) {
        skip = test.skipFailedInvocationCounts();
      }
      TestRunner testRunner = new TestRunner(m_configuration, suite, test,
          suite.getOutputDirectory(), suite.getAnnotationFinder(), skip,
          listeners, classListeners);

      if (m_useDefaultListeners) {
        testRunner.addListener(new TestHTMLReporter());
        testRunner.addListener(new JUnitXMLReporter());

        //TODO: Moved these here because maven2 has output reporters running
        //already, the output from these causes directories to be created with
        //files. This is not the desired behaviour of running tests in maven2.
        //Don't know what to do about this though, are people relying on these
        //to be added even with defaultListeners set to false?
        testRunner.addListener(new TextReporter(testRunner.getName(), TestRunner.getVerbose()));
      }

      for (ITestListener itl : m_failureGenerators) {
        testRunner.addListener(itl);
      }
      for (IConfigurationListener cl : m_configuration.getConfigurationListeners()) {
        testRunner.addConfigurationListener(cl);
      }

      return testRunner;
    }
  }

  private static class ProxyTestRunnerFactory implements ITestRunnerFactory {
    private ITestListener[] m_failureGenerators;
    private ITestRunnerFactory m_target;

    public ProxyTestRunnerFactory(ITestListener[] failureListeners, ITestRunnerFactory target) {
      m_failureGenerators = failureListeners;
      m_target= target;
    }

    @Override
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        Collection<IInvokedMethodListener> listeners, List<IClassListener> classListeners) {
      TestRunner testRunner= m_target.newTestRunner(suite, test, listeners, classListeners);

      testRunner.addListener(new TextReporter(testRunner.getName(), TestRunner.getVerbose()));

      for (ITestListener itl : m_failureGenerators) {
        testRunner.addListener(itl);
      }

      return testRunner;
    }
  }

  public void setHost(String host) {
    m_host = host;
  }

  @Override
  public String getHost() {
    return m_host;
  }

  private SuiteRunState m_suiteState= new SuiteRunState();

  /**
   * @see org.testng.ISuite#getSuiteState()
   */
  @Override
  public SuiteRunState getSuiteState() {
    return m_suiteState;
  }

  public void setSkipFailedInvocationCounts(Boolean skipFailedInvocationCounts) {
    if (skipFailedInvocationCounts != null) {
      m_skipFailedInvocationCounts = skipFailedInvocationCounts;
    }
  }

  private IAttributes m_attributes = new Attributes();

  @Override
  public Object getAttribute(String name) {
    return m_attributes.getAttribute(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    m_attributes.setAttribute(name, value);
  }

  @Override
  public Set<String> getAttributeNames() {
    return m_attributes.getAttributeNames();
  }

  @Override
  public Object removeAttribute(String name) {
    return m_attributes.removeAttribute(name);
  }

  /////
  // implements IInvokedMethodListener
  //

  @Override
  public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
  }

  @Override
  public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
    if (method == null) {
      throw new NullPointerException("Method should not be null");
    }
    m_invokedMethods.add(method);
  }

  //
  // implements IInvokedMethodListener
  /////

  @Override
  public List<IInvokedMethod> getAllInvokedMethods() {
    return m_invokedMethods;
  }

  @Override
  public List<ITestNGMethod> getAllMethods() {
    return m_allTestMethods;
  }
}
