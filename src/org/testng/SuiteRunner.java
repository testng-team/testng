package org.testng;


import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.AnnotationTypeEnum;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <CODE>SuiteRunner</CODE> is responsible for running all the tests included in one
 * suite. The test start is triggered by {@link #run()} method.
 *
 * @author Cedric Beust, Apr 26, 2004
 */
public class SuiteRunner implements ISuite, Serializable {
  
  /* generated */
  private static final long serialVersionUID = 5284208932089503131L;
  
  private static final String DEFAULT_OUTPUT_DIR = "test-output";

  private Map<String, ISuiteResult> m_suiteResults = Maps.newHashMap();
  transient private List<TestRunner> m_testRunners = Lists.newArrayList();
  transient private List<ISuiteListener> m_listeners = Lists.newArrayList();
  transient private TestListenerAdapter m_textReporter = new TestListenerAdapter();

  private String m_outputDir; // DEFAULT_OUTPUT_DIR;
  private XmlSuite m_suite;

  transient private List<ITestListener> m_testlisteners = Lists.newArrayList();
  transient private ITestRunnerFactory m_tmpRunnerFactory;

  transient private ITestRunnerFactory m_runnerFactory;
  transient private boolean m_useDefaultListeners = true;
  
  // The remote host where this suite was run, or null if run locally
  private String m_host;

  // The suite can run in some cases both JAVADOC and JDK5 annotated tests
  transient private IAnnotationFinder m_jdkAnnotationFinder;
  
  transient private IObjectFactory m_objectFactory;
  transient private Boolean m_skipFailedInvocationCounts = Boolean.FALSE;

  private IMethodInterceptor m_methodInterceptor;
  private List<IInvokedMethodListener> m_invokedMethodListeners;
  
//  transient private IAnnotationTransformer m_annotationTransformer = null;

  public SuiteRunner(XmlSuite suite, String outputDir, IAnnotationFinder finder) 
  {
    this(suite, outputDir, null, false, finder);
  }

  public SuiteRunner(XmlSuite suite, String outputDir, 
      ITestRunnerFactory runnerFactory, IAnnotationFinder finder) 
  {
    this(suite, outputDir, runnerFactory, false, finder);
  }
  
  public SuiteRunner(XmlSuite suite, 
                     String outputDir, 
                     ITestRunnerFactory runnerFactory, 
                     boolean useDefaultListeners,
                     IAnnotationFinder finder)
  {
    this(suite, outputDir, runnerFactory, useDefaultListeners, finder, null, null,
        null);
  }
  
  public SuiteRunner(XmlSuite suite, 
                     String outputDir, 
                     ITestRunnerFactory runnerFactory, 
                     boolean useDefaultListeners,
                     IAnnotationFinder finder,
                     IObjectFactory factory,
                     IMethodInterceptor methodInterceptor,
                     List<IInvokedMethodListener> invokedMethodListener)
  {
    init(suite, outputDir, runnerFactory, useDefaultListeners, finder, factory,
      methodInterceptor, invokedMethodListener);
  }
  
  private void init(XmlSuite suite, 
                    String outputDir, 
                    ITestRunnerFactory runnerFactory, 
                    boolean useDefaultListeners,
                    IAnnotationFinder finder, 
                    IObjectFactory factory,
                    IMethodInterceptor methodInterceptor,
                    List<IInvokedMethodListener> invokedMethodListener)
  {
    m_suite = suite;
    m_useDefaultListeners = useDefaultListeners;
    m_tmpRunnerFactory= runnerFactory;
    m_methodInterceptor = methodInterceptor;
    m_jdkAnnotationFinder = finder;
    setOutputDir(outputDir);
    m_objectFactory = factory;
    if(m_objectFactory == null) {
      m_objectFactory = suite.getObjectFactory();
    }
    m_invokedMethodListeners = invokedMethodListener;
  }
  
  public XmlSuite getXmlSuite() {
    return m_suite;
  }

  public String getName() {
    return m_suite.getName();
  }

  public void setObjectFactory(IObjectFactory objectFactory) {
    m_objectFactory = objectFactory;
  }

  public void setTestListeners(List<ITestListener> testlisteners) {
    m_testlisteners = testlisteners;
  }

  public void setReportResults(boolean reportResults) {
    m_useDefaultListeners = reportResults;
  }
  
  private void invokeListeners(boolean start) {
    for (ISuiteListener sl : m_listeners) {
      if (start) {
        sl.onStart(this);
      }
      else {
        sl.onFinish(this);
      }
    }
  }

  private void setOutputDir(String outputdir) {
    if (((null == outputdir) || "".equals(outputdir.trim()))
        && m_useDefaultListeners) {
      outputdir= DEFAULT_OUTPUT_DIR;
    }
    
    m_outputDir = (null != outputdir) ? new File(outputdir).getAbsolutePath()
        : null;
  }

  private void lazyInit() {
    m_runnerFactory = buildRunnerFactory(m_testlisteners);
  }

  protected ITestRunnerFactory buildRunnerFactory(List testListeners) {
    ITestRunnerFactory factory = null;
    
    if (null == m_tmpRunnerFactory) {
      factory = new DefaultTestRunnerFactory(
          m_testlisteners.toArray(new ITestListener[m_testlisteners.size()]), 
          m_useDefaultListeners, m_skipFailedInvocationCounts);
    }
    else {
      factory = new ProxyTestRunnerFactory(
          m_testlisteners.toArray(new ITestListener[m_testlisteners.size()]), 
          m_tmpRunnerFactory);
    }
    
    return factory;
  }
  
  public String getParallel() {
    return m_suite.getParallel();
  }

  public void run() {
    lazyInit();

    invokeListeners(true /* start */);
    try {
      privateRun();
    }
    finally {
      invokeListeners(false /* start */);

      //
      // Display the final statistics
      //
      if (m_suite.getVerbose() > 0) {
        int total = m_textReporter.getAllTestMethods().length;
        List<ITestResult> skipped = m_textReporter.getSkippedTests();
        List<ITestResult> failed = m_textReporter.getFailedTests();
        int confFailures= m_textReporter.getConfigurationFailures().size();
        int confSkips= m_textReporter.getConfigurationSkips().size();
        StringBuffer bufLog= new StringBuffer(getName());
        bufLog.append("\nTotal tests run: ")
            .append(total)
            .append(", Failures: ").append(failed.size())
            .append(", Skips: ").append(skipped.size());;
        if(confFailures > 0 || confSkips > 0) {
          bufLog.append("\nConfiguration Failures: ").append(confFailures)
              .append(", Skips: ").append(confSkips)
              ;
        }
            
         System.out.println("\n===============================================\n"
                           + bufLog.toString()
                           + "\n===============================================\n");
      }
    }
  }

  private void privateRun() {
    
    // Map for unicity, Linked for guaranteed order
    Map<Method, ITestNGMethod> beforeSuiteMethods= new LinkedHashMap<Method, ITestNGMethod>();
    Map<Method, ITestNGMethod> afterSuiteMethods = new LinkedHashMap<Method, ITestNGMethod>();

    IInvoker invoker = null;
    
    //
    // First, we create all the test runners so we can discover all the ITestClasses
    //
    for (XmlTest test : m_suite.getTests()) {


      TestRunner tr = m_runnerFactory.newTestRunner(this, test, m_invokedMethodListeners);
      
      //
      // Install the method interceptor, if any was passed
      //
      if (m_methodInterceptor != null) {
        tr.setMethodInterceptor(m_methodInterceptor);
      }

      // Reuse the same text reporter so we can accumulate all the results
      // (this is used to display the final suite report at the end)
      tr.addListener(m_textReporter);
      m_testRunners.add(tr);

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
      boolean testsInParallel = XmlSuite.PARALLEL_TESTS.equals(m_suite.getParallel());
      if (!testsInParallel) {
        runSequentially();
      } 
      else {
        runConcurrently();
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


  private void runConcurrently() {
    List<Runnable> tasks= Lists.newArrayList(m_testRunners.size());
    for(TestRunner tr: m_testRunners) {
      tasks.add(new SuiteWorker(tr));
    }
    
    ThreadUtil.execute(tasks, m_suite.getThreadCount(), m_suite.getTimeOut(m_suite.getTests().size() * 1000L), false);
  }

  private class SuiteWorker implements Runnable {
      private TestRunner m_testRunner;

      public SuiteWorker(TestRunner tr) {
        m_testRunner = tr;
      }

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
  public void addListener(ISuiteListener reporter) {
    m_listeners.add(reporter);
  }

  public String getOutputDirectory() {
    return m_outputDir + File.separatorChar + getName();
  }

  public Map<String, ISuiteResult> getResults() {
    return m_suiteResults;
  }
  
  /**
   * FIXME: should be removed?
   *
   * @see org.testng.ISuite#getParameter(java.lang.String)
   */
  public String getParameter(String parameterName) {
    return m_suite.getParameter(parameterName);
  }

  /**
   * @see org.testng.ISuite#getMethodsByGroups()
   */
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
  public Collection<ITestNGMethod> getInvokedMethods() {
    return getIncludedOrExcludedMethods(true /* included */);
  }

  /**
   * @see org.testng.ISuite#getExcludedMethods()
   */
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

  public IObjectFactory getObjectFactory() {
    return m_objectFactory;
  }

  /**
   * Returns the annotation finder for the given annotation type.
   * @param pAnnotationType the annotation type 
   * @return the annotation finder for the given annotation type. 
   */
  public IAnnotationFinder getAnnotationFinder(String pAnnotationType) 
  {
    AnnotationTypeEnum annotationType = AnnotationTypeEnum.valueOf(pAnnotationType);
    if (AnnotationTypeEnum.JDK != annotationType) {
      throw new TestNGException("Javadoc annotations are no longer supported. Either" +
      		" update your tests to JDK annotations or use TestNG 5.11.");
    }
    return m_jdkAnnotationFinder;
  }

  public static void ppp(String s) {
    System.out.println("[SuiteRunner] " + s);
  }

  /**
   * The default implementation of {@link ITestRunnerFactory}.
   */
  public static class DefaultTestRunnerFactory implements ITestRunnerFactory {
    private ITestListener[] m_failureGenerators;
    private boolean m_useDefaultListeners;
    private boolean m_skipFailedInvocationCounts;
    
    public DefaultTestRunnerFactory(ITestListener[] failureListeners,
        boolean useDefaultListeners,
        boolean skipFailedInvocationCounts)
    {
      m_failureGenerators = failureListeners;
      m_useDefaultListeners = useDefaultListeners;
      m_skipFailedInvocationCounts = skipFailedInvocationCounts;
    }

    /**
     * @see ITestRunnerFactory#newTestRunner(org.testng.ISuite, org.testng.xml.XmlTest)
     */
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        List<IInvokedMethodListener> listeners) {
      boolean skip = m_skipFailedInvocationCounts;
      if (! skip) {
        skip = test.skipFailedInvocationCounts();
      }
      TestRunner testRunner = 
        new TestRunner(suite,
                        test,
                        suite.getOutputDirectory(),
                        suite.getAnnotationFinder(test.getAnnotations()),
                        skip,
                        listeners);
      
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
      
      return testRunner;
    }
  }

  public static class ProxyTestRunnerFactory implements ITestRunnerFactory {
    private ITestListener[] m_failureGenerators;
    private ITestRunnerFactory m_target;

    public ProxyTestRunnerFactory(ITestListener[] failureListeners, ITestRunnerFactory target) {
      m_failureGenerators = failureListeners;
      m_target= target;
    }

    /**
     * @see ITestRunnerFactory#newTestRunner(org.testng.ISuite, org.testng.xml.XmlTest)
     */
    public TestRunner newTestRunner(ISuite suite, XmlTest test,
        List<IInvokedMethodListener> listeners) {
      TestRunner testRunner= m_target.newTestRunner(suite, test, listeners);

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
  
  public String getHost() {
    return m_host;
  }

  private SuiteRunState m_suiteState= new SuiteRunState();

  /**
   * @see org.testng.ISuite#getSuiteState()
   */
  public SuiteRunState getSuiteState() {
    return m_suiteState;
  }

  public void setSkipFailedInvocationCounts(Boolean skipFailedInvocationCounts) {
    if (skipFailedInvocationCounts != null) {
      m_skipFailedInvocationCounts = skipFailedInvocationCounts;
    }
  }

  private Map<String, Object> m_attributes = Maps.newHashMap();

  public Object getAttribute(String name) {
    return m_attributes.get(name);
  }

  public void setAttribute(String name, Object value) {
    m_attributes.put(name, value);
  }

}
