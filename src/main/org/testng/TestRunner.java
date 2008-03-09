package org.testng;


import org.testng.internal.ClassHelper;
import org.testng.internal.ConfigurationGroupMethods;
import org.testng.internal.Constants;
import org.testng.internal.IConfigurationListener;
import org.testng.internal.IInvoker;
import org.testng.internal.IMethodWorker;
import org.testng.internal.ITestResultNotifier;
import org.testng.internal.InvokedMethod;
import org.testng.internal.Invoker;
import org.testng.internal.MethodHelper;
import org.testng.internal.MethodInstance;
import org.testng.internal.ResultMap;
import org.testng.internal.RunInfo;
import org.testng.internal.TestMethodWorker;
import org.testng.internal.TestNGClassFinder;
import org.testng.internal.TestNGMethodFinder;
import org.testng.internal.Utils;
import org.testng.internal.XmlMethodSelector;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.thread.ThreadUtil;
import org.testng.junit.IJUnitTestRunner;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class takes care of running one Test.
 *
 * @author Cedric Beust, Apr 26, 2004
 * @author <a href = "mailto:the_mindstorm&#64;evolva.ro">Alexandru Popescu</a>
 */
public class TestRunner implements ITestContext, ITestResultNotifier {
  /* generated */
  private static final long serialVersionUID = 4247820024988306670L;
  private ISuite m_suite;
  protected XmlTest m_xmlTest;
  private String m_testName;

  transient private List<XmlClass> m_testClassesFromXml= null;
  transient private List<XmlPackage> m_packageNamesFromXml= null;

  transient private IInvoker m_invoker= null;
  transient private IAnnotationFinder m_annotationFinder= null;

  /** ITestListeners support. */
  transient private List<ITestListener> m_testListeners = new ArrayList<ITestListener>();
  transient private List<IConfigurationListener> m_configurationListeners= new ArrayList<IConfigurationListener>();

  transient private IConfigurationListener m_confListener= new ConfigurationListener();
  transient private boolean m_skipFailedInvocationCounts;
  
  /**
   * All the test methods we found, associated with their respective classes.
   * Note that these test methods might belong to different classes.
   * We pick which ones to run at runtime.
   */
  private ITestNGMethod[] m_allTestMethods = new ITestNGMethod[0];

  // Information about this test run

  private Date m_startDate = null;
  private Date m_endDate = null;

  /** A map to keep track of Class <-> IClass. */
  transient private Map<Class<?>, ITestClass> m_classMap= new HashMap<Class<?>, ITestClass>();

  /** Where the reports will be created. */
  private String m_outputDirectory= Constants.getDefaultValueFor(Constants.PROP_OUTPUT_DIR);

  // The XML method selector (groups/methods included/excluded in XML)
  private XmlMethodSelector m_xmlMethodSelector = new XmlMethodSelector();

  private static int m_verbose = 1;

  //
  // These next fields contain all the configuration methods found on this class.
  // At initialization time, they just contain all the various @Configuration methods
  // found in all the classes we are going to run.  When comes the time to run them,
  // only a subset of them are run:  those that are enabled and belong on the same class as
  // (or a parent of) the test class.
  //
  /** */
  private ITestNGMethod[] m_beforeSuiteMethods = {};
  private ITestNGMethod[] m_afterSuiteMethods = {};
  private ITestNGMethod[] m_beforeXmlTestMethods = {};
  private ITestNGMethod[] m_afterXmlTestMethods = {};
  private List<ITestNGMethod> m_excludedMethods = new ArrayList<ITestNGMethod>();
  private ConfigurationGroupMethods m_groupMethods = null;

  // Meta groups
  private Map<String, List<String>> m_metaGroups = new HashMap<String, List<String>>();

  // All the tests that were run along with their result
  private IResultMap m_passedTests = new ResultMap();
  private IResultMap m_failedTests = new ResultMap();
  private IResultMap m_failedButWithinSuccessPercentageTests = new ResultMap();
  private IResultMap m_skippedTests = new ResultMap();

  private RunInfo m_runInfo= new RunInfo();
  
  // The host where this test was run, or null if run locally
  private String m_host;

  private Map<String, Object> m_attributes = new HashMap<String, Object>();
  private IMethodInterceptor m_methodInterceptor = new IMethodInterceptor() {

  public List<IMethodInstance> intercept(List<IMethodInstance> methods,
        ITestContext context)
    {
      Collections.sort(methods, MethodInstance.SORT_BY_CLASS);
      return methods;
    }
    
  };

  public TestRunner(ISuite suite,
                    XmlTest test,
                    String outputDirectory,
                    IAnnotationFinder finder,
                    boolean skipFailedInvocationCounts) 
  {
    init(suite, test, outputDirectory, finder, skipFailedInvocationCounts);
  }

  public TestRunner(ISuite suite, XmlTest test, 
    IAnnotationFinder finder, boolean skipFailedInvocationCounts) 
  {
    init(suite, test, suite.getOutputDirectory(), finder, skipFailedInvocationCounts);
  }

  public TestRunner(ISuite suite, XmlTest test, 
    boolean skipFailedInvocationCounts)
  {
    init(suite, test, suite.getOutputDirectory(), 
        suite.getAnnotationFinder(test.getAnnotations()),
        skipFailedInvocationCounts);
  }

  private void init(ISuite suite,
                    XmlTest test,
                    String outputDirectory,
                    IAnnotationFinder annotationFinder,
                    boolean skipFailedInvocationCounts)
  {
    m_xmlTest= test;
    m_suite = suite;
    m_testName = test.getName();
    m_host = suite.getHost();
    m_testClassesFromXml= test.getXmlClasses();
    m_skipFailedInvocationCounts = skipFailedInvocationCounts;
    
    m_packageNamesFromXml= test.getXmlPackages();    
    if(null != m_packageNamesFromXml) {
      for(XmlPackage xp: m_packageNamesFromXml) {
        m_testClassesFromXml.addAll(xp.getXmlClasses());
      }
    }

    m_annotationFinder= annotationFinder;
    m_invoker = 
      new Invoker(this, this, m_suite.getSuiteState(), 
        m_annotationFinder, m_skipFailedInvocationCounts);

    setVerbose(test.getVerbose());

    if (suite.getParallel() != null) {
      log(3, "Running the tests in '" + test.getName() + "' with parallel mode:" + suite.getParallel());
    } 

    setOutputDirectory(outputDirectory);

    // Finish our initialization
    init();
  }

  public IInvoker getInvoker() {
    return m_invoker;
  }

  public ITestNGMethod[] getBeforeSuiteMethods() {
    return m_beforeSuiteMethods;
  }

  public ITestNGMethod[] getAfterSuiteMethods() {
    return m_afterSuiteMethods;
  }

  public ITestNGMethod[] getBeforeTestConfigurationMethods() {
    return m_beforeXmlTestMethods;
  }

  public ITestNGMethod[] getAfterTestConfigurationMethods() {
    return m_afterXmlTestMethods;
  }

  public Object getAttribute(String name) {
    return m_attributes.get(name);
  }

  public void setAttribute(String name, Object value) {
    m_attributes.put(name, value);
  }

  private void init() {
    initMetaGroups(m_xmlTest);
    initRunInfo(m_xmlTest);

    // Init methods and class map
    // JUnit behavior is different and doesn't need this initialization step
    if(!m_xmlTest.isJUnit()) {
      initMethods();
    }

    addConfigurationListener(m_confListener);
  }

  /**
   * Initialize meta groups
   */
  private void initMetaGroups(XmlTest xmlTest) {
    Map<String, List<String>> metaGroups = xmlTest.getMetaGroups();

    for (String name : metaGroups.keySet()) {
      addMetaGroup(name, metaGroups.get(name));
    }
  }

  private void initRunInfo(final XmlTest xmlTest) {
    // Groups
    m_xmlMethodSelector.setIncludedGroups(createGroups(m_xmlTest.getIncludedGroups()));
    m_xmlMethodSelector.setExcludedGroups(createGroups(m_xmlTest.getExcludedGroups()));
    m_xmlMethodSelector.setExpression(m_xmlTest.getExpression());

    // Methods
    m_xmlMethodSelector.setXmlClasses(m_xmlTest.getXmlClasses());
    
    m_runInfo.addMethodSelector(m_xmlMethodSelector, 10);
    
    // Add user-specified method selectors (only class selectors, we can ignore
    // script selectors here)
    if (null != xmlTest.getMethodSelectors()) {
      for (org.testng.xml.XmlMethodSelector selector : xmlTest.getMethodSelectors()) {
        if (selector.getClassName() != null) {
          IMethodSelector s = ClassHelper.createSelector(selector);
          
          m_runInfo.addMethodSelector(s, selector.getPriority());
        }
      }
    }
  }

  private void initMethods() {

    //
    // Calculate all the methods we need to invoke
    //
    List<ITestNGMethod> beforeClassMethods = new ArrayList<ITestNGMethod>();
    List<ITestNGMethod> testMethods = new ArrayList<ITestNGMethod>();
    List<ITestNGMethod> afterClassMethods = new ArrayList<ITestNGMethod>();
    List<ITestNGMethod> beforeSuiteMethods = new ArrayList<ITestNGMethod>();
    List<ITestNGMethod> afterSuiteMethods = new ArrayList<ITestNGMethod>();
    List<ITestNGMethod> beforeXmlTestMethods = new ArrayList<ITestNGMethod>();
    List<ITestNGMethod> afterXmlTestMethods = new ArrayList<ITestNGMethod>();

    ITestClassFinder testClassFinder= new TestNGClassFinder(Utils.xmlClassesToClasses(m_testClassesFromXml),
                                             null,
                                             m_xmlTest,
                                             m_annotationFinder,
                                             this);
    ITestMethodFinder testMethodFinder
      = new TestNGMethodFinder<ITestNGMethod>(m_runInfo, m_annotationFinder);
    
    m_runInfo.setTestMethods(testMethods);
    
    //
    // Initialize TestClasses
    //
    IClass[] classes = testClassFinder.findTestClasses();
    
    for (IClass ic : classes) {

      // Create TestClass
      ITestClass tc = new TestClass(ic,
                                   m_testName,
                                   testMethodFinder,
                                   m_annotationFinder,
                                   m_runInfo);
      m_classMap.put(ic.getRealClass(), tc);
    }
    
    //
    // Calculate groups methods
    //
    Map<String, List<ITestNGMethod>> beforeGroupMethods= MethodHelper.findGroupsMethods(m_classMap.values(), true);
    Map<String, List<ITestNGMethod>> afterGroupMethods= MethodHelper.findGroupsMethods(m_classMap.values(), false);

    //
    // Walk through all the TestClasses, store their method
    // and initialize them with the correct ITestClass
    //
    for (ITestClass tc : m_classMap.values()) {
      fixMethodsWithClass(tc.getBeforeClassMethods(), tc, beforeClassMethods);
      fixMethodsWithClass(tc.getBeforeTestMethods(), tc, null); // HINT: does not link back to beforeTestMethods
      fixMethodsWithClass(tc.getTestMethods(), tc, testMethods);
      fixMethodsWithClass(tc.getAfterTestMethods(), tc, null);
      fixMethodsWithClass(tc.getAfterClassMethods(), tc, afterClassMethods);
      fixMethodsWithClass(tc.getBeforeSuiteMethods(), tc, beforeSuiteMethods);
      fixMethodsWithClass(tc.getAfterSuiteMethods(), tc, afterSuiteMethods);
      fixMethodsWithClass(tc.getBeforeTestConfigurationMethods(), tc, beforeXmlTestMethods);
      fixMethodsWithClass(tc.getAfterTestConfigurationMethods(), tc, afterXmlTestMethods);
      fixMethodsWithClass(tc.getBeforeGroupsMethods(), tc, 
          MethodHelper.uniqueMethodList(beforeGroupMethods.values()));
      fixMethodsWithClass(tc.getAfterGroupsMethods(), tc, 
          MethodHelper.uniqueMethodList(afterGroupMethods.values()));
    }

    //
    // Sort the methods
    //
    m_beforeSuiteMethods = MethodHelper.collectAndOrderConfigurationMethods(beforeSuiteMethods,
                                                              m_runInfo,
                                                              m_annotationFinder,
                                                              true /* unique */,
                                                              m_excludedMethods);

    m_beforeXmlTestMethods = MethodHelper.collectAndOrderConfigurationMethods(beforeXmlTestMethods,
                                                                m_runInfo,
                                                                m_annotationFinder,
                                                                true, // CQ added by me
                                                                m_excludedMethods);

    m_allTestMethods = MethodHelper.collectAndOrderMethods(testMethods,
                                                          m_runInfo,
                                                          m_annotationFinder,
                                                          m_excludedMethods);

    m_afterXmlTestMethods = MethodHelper.collectAndOrderConfigurationMethods(afterXmlTestMethods,
                                                               m_runInfo,
                                                               m_annotationFinder,
                                                               true, // CQ added by me
                                                               m_excludedMethods);

    m_afterSuiteMethods = MethodHelper.collectAndOrderConfigurationMethods(afterSuiteMethods,
                                                             m_runInfo,
                                                             m_annotationFinder,
                                                             true /* unique */,
                                                             m_excludedMethods);
    // shared group methods
    m_groupMethods = new ConfigurationGroupMethods(m_allTestMethods, beforeGroupMethods, afterGroupMethods);
  }

  private void fixMethodsWithClass(ITestNGMethod[] methods,
                                   ITestClass testCls,
                                   List<ITestNGMethod> methodList) {
    for (ITestNGMethod itm : methods) {
      itm.setTestClass(testCls);

      if (methodList != null) {
        methodList.add(itm);
      }
    }
  }

  public Collection<ITestClass> getIClass() {
    return m_classMap.values();
  }

  public void setTestName(String name) {
    m_testName = name;
  }

  public void setOutputDirectory(String od) {
    m_outputDirectory= od;
//  FIX: empty directories were created
//    if (od == null) { m_outputDirectory = null; return; } //for maven2
//    File file = new File(od);
//    file.mkdirs();
//    m_outputDirectory= file.getAbsolutePath();
  }

  private void addMetaGroup(String name, List<String> groupNames) {
    m_metaGroups.put(name, groupNames);
  }

  /**
   * Calculate the transitive closure of all the MetaGroups
   *
   * @param groups
   * @param unfinishedGroups
   * @param result           The transitive closure containing all the groups found
   */
  private void collectGroups(String[] groups,
                             List<String> unfinishedGroups,
                             Map<String, String> result) {
    for (String gn : groups) {
      List<String> subGroups = m_metaGroups.get(gn);
      if (null != subGroups) {

        for (String sg : subGroups) {
          if (null == result.get(sg)) {
            result.put(sg, sg);
            unfinishedGroups.add(sg);
          }
        }
      }
    }
  }

  private Map<String, String> createGroups(List<String> groups) {
    return createGroups((String[]) groups.toArray(new String[groups.size()]));
  }

  private Map<String, String> createGroups(String[] groups) {
    Map<String, String> result= new HashMap<String, String>();

    // Groups that were passed on the command line
    for (String group : groups) {
      result.put(group, group);
    }

    // See if we have any MetaGroups and
    // expand them if they match one of the groups
    // we have just been passed
    List<String> unfinishedGroups = new ArrayList<String>();

    if (m_metaGroups.size() > 0) {
      collectGroups(groups, unfinishedGroups, result);

      // Do we need to loop over unfinished groups?
      while (unfinishedGroups.size() > 0) {
        String[] uGroups = (String[]) unfinishedGroups.toArray(new String[unfinishedGroups.size()]);
        unfinishedGroups = new ArrayList<String>();
        collectGroups(uGroups, unfinishedGroups, result);
      }
    }

    //    Utils.dumpMap(result);
    return result;
  }

  /**
   * The main entry method for TestRunner.
   *
   * This is where all the hard work is done:
   * - Invoke configuration methods
   * - Invoke test methods
   * - Catch exceptions
   * - Collect results
   * - Invoke listeners
   * - etc...
   */
  public void run() {
    beforeRun();

    try {
      XmlTest test= getTest();
      if(test.isJUnit()) {
        privateRunJUnit(test);
      }
      else {
        privateRun(test);
      }
    }
    finally {
      afterRun();
    }
  }

  /** Before run preparements. */
  private void beforeRun() {
    //
    // Log the start date
    //
    m_startDate = new Date(System.currentTimeMillis());

    // Log start
    logStart();

    // Invoke listeners
    fireEvent(true /*start*/);
    
    // invoke @BeforeTest
    ITestNGMethod[] testConfigurationMethods= getBeforeTestConfigurationMethods();
    if(null != testConfigurationMethods && testConfigurationMethods.length > 0) {
      m_invoker.invokeConfigurations(null,
                                     testConfigurationMethods,
                                     m_xmlTest.getSuite(), 
                                     m_xmlTest.getParameters(),
                                     null /* instance */);
    }
  }

  private void privateRunJUnit(XmlTest xmlTest) {
    final Class<?>[] classes= Utils.xmlClassesToClasses(m_testClassesFromXml);
    final List<ITestNGMethod> runMethods= new ArrayList<ITestNGMethod>();
    List<IMethodWorker> workers= new ArrayList<IMethodWorker>();
    // FIXME: directly referincing JUnitTestRunner which uses JUnit classes
    // may result in an class resolution exception under different JVMs
    // The resolution process is not specified in the JVM spec with a specific implementation,
    // so it can be eager => failure
    workers.add(new IMethodWorker() {
      /**
       * @see org.testng.internal.IMethodWorker#getMaxTimeOut()
       */
      public long getMaxTimeOut() {
        return 0;
      }
            
      public List<ITestResult> getTestResults() {
        return null;
      }

      /**
       * @see java.lang.Runnable#run()
       */
      public void run() {    
        for(Class<?> tc: classes) {
          IJUnitTestRunner tr= ClassHelper.createTestRunner(TestRunner.this);
          try {
            tr.run(tc);
          }
          catch(Exception ex) {
            ex.printStackTrace();
          }
          finally {
            runMethods.addAll(tr.getTestMethods());
          }
        }
      }
    });

    runWorkers(workers, "" /* JUnit does not support parallel */);
    m_allTestMethods= runMethods.toArray(new ITestNGMethod[runMethods.size()]);
  }
  
  public void privateRun(XmlTest xmlTest) {
    //
    // Calculate the lists of tests that can be run in sequence and in parallel
    //
    List<List<ITestNGMethod>> sequentialList= new ArrayList<List<ITestNGMethod>>();
    List<ITestNGMethod> parallelList= new ArrayList<ITestNGMethod>();

    computeTestLists(sequentialList, parallelList);
    
    log(3, "Found " + (sequentialList.size() + parallelList.size()) + " applicable methods");
    
    //
    // Create the workers
    //
    List<TestMethodWorker> workers = new ArrayList<TestMethodWorker>();
    
    ClassMethodMap cmm = new ClassMethodMap(m_allTestMethods);
    
    createSequentialWorkers(sequentialList, xmlTest.getParameters(), cmm, workers);

    // All the parallel tests are placed in a separate worker, so they can be
    // invoked in parallel
    createParallelWorkers(parallelList, xmlTest.getParameters(), cmm, workers);
    
//    m_testPlan =
//      new TestPlan(sequentialList, parallelList, cmm,
//        getBeforeSuiteMethods(), getAfterSuiteMethods(),
//        m_groupMethods, xmlTest);

    try {
      runWorkers(workers, xmlTest.getParallel());
    }
    finally {
      cmm.clear();
    }
  }
  
//  private TestPlan m_testPlan;
//  private IRunGroupFactory m_runGroupFactory;
//
//  public TestPlan getTestPlan() {
//    return m_testPlan;
//  }

  private void createParallelWorkers(List<ITestNGMethod> parallel, 
      Map<String, String> params, ClassMethodMap cmm, List<TestMethodWorker> workers) {

    if(parallel.isEmpty()) return;
    
    List<IMethodInstance> methodInstances = new ArrayList<IMethodInstance>();
    for (ITestNGMethod tm : parallel) {
      methodInstances.addAll(methodsToMultipleMethodInstances(tm));
    }
    
    //
    // Finally, sort the parallel methods by classes
    //
    methodInstances = m_methodInterceptor.intercept(methodInstances, this);

    if (getVerbose() >= 2) {
      log(3, "WILL BE RUN IN RANDOM ORDER:");
      for (IMethodInstance mi : methodInstances) {
        log(3, "  " + mi.getMethod());
        log(3, "      on instances");
        for(Object o: mi.getInstances()) {
          log(3, "     " + o);
        }
      }
      log(3, "===");
    }

    for (IMethodInstance mi : methodInstances) {
      workers.add(new TestMethodWorker(m_invoker,
                                       new IMethodInstance[] { mi },
                                       m_xmlTest.getSuite(),
                                       params,
                                       m_allTestMethods,
                                       m_groupMethods,
                                       cmm,
                                       this));
    }

  }
  
  private void createSequentialWorkers(List<List<ITestNGMethod>> sequentialList, 
      Map<String, String> params, ClassMethodMap cmm, List<TestMethodWorker> workers) {
    if(sequentialList.isEmpty()) return;
    
    // All the sequential tests are place in one worker, guaranteeing they
    // will be invoked sequentially
    for (List<ITestNGMethod> sl : sequentialList) {        
      workers.add(new TestMethodWorker(m_invoker,
                                       methodsToMethodInstances(sl),
                                       m_xmlTest.getSuite(),
                                       params,
                                       m_allTestMethods,
                                       m_groupMethods,
                                       cmm,
                                       this));
    }
    if (getVerbose() >= 2) {
      log(3, "WILL BE RUN SEQUENTIALLY:");
      for (List<ITestNGMethod> l : sequentialList) {
        for (ITestNGMethod tm : l) {
          log(3, "  " + tm);
        }
        log(3, "====");
      }
      
      log(3, "===");
    }
  }
  
  private List<MethodInstance> methodsToMultipleMethodInstances(ITestNGMethod... sl) {
    List<MethodInstance> vResult = new ArrayList<MethodInstance>();
    for (ITestNGMethod m : sl) {
      Object[] instances = m.getTestClass().getInstances(true);
      for (Object instance : instances) {
        vResult.add(new MethodInstance(m, new Object[] { instance }));
      }
    }
    
    return vResult;
  }

  private MethodInstance[] methodsToMethodInstances(List<ITestNGMethod> sl) {
    MethodInstance[] result = new MethodInstance[sl.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = new MethodInstance(sl.get(i), sl.get(i).getTestClass().getInstances(true));
    }
    
    return result;
  }

  //
  // Invoke the workers
  //
  private void runWorkers(List<? extends IMethodWorker> workers, String parallelMode) {
    if (XmlSuite.PARALLEL_METHODS.equals(parallelMode) 
        || "true".equalsIgnoreCase(parallelMode) ) 
    {
      //
      // Parallel run
      //
      // Default timeout for individual methods:  10 seconds
      long maxTimeOut = m_xmlTest.getTimeOut(10 * 1000);
      for (IMethodWorker tmw : workers) {
        long mt= tmw.getMaxTimeOut();
        if (mt > maxTimeOut) {
          maxTimeOut= mt;
        }
      }

      ThreadUtil.execute(workers, m_xmlTest.getThreadCount(), maxTimeOut, false);
    }
    else {
      //
      // Sequential run
      //
      for (IMethodWorker tmw : workers) {
        tmw.run();
      }
    }
  }
  
  private void afterRun() {
    // invoke @AfterTest
    ITestNGMethod[] testConfigurationMethods= getAfterTestConfigurationMethods();
    if(null != testConfigurationMethods && testConfigurationMethods.length > 0) {
      m_invoker.invokeConfigurations(null,
                                     testConfigurationMethods,
                                     m_xmlTest.getSuite(), 
                                     m_xmlTest.getParameters(),
                                     null /* instance */);
    }

    //
    // Log the end date
    //
    m_endDate = new Date(System.currentTimeMillis());
    
    if (getVerbose() >= 3) {
      dumpInvokedMethods();
    }

    // Invoke listeners
    fireEvent(false /*stop*/);

    // Statistics
//    logResults();
  }
  
  /**
   * @param regexps
   * @param group
   * @return true if the map contains at least one regexp that matches the 
   * given group
   */
  private boolean containsString(Map<String, String> regexps, String group) {
    for (String regexp : regexps.values()) {
      boolean match = Pattern.matches(regexp, group);
      if (match) {
        return true;
      }
    }
    
    return false;
  }

  /**
   * Creates the
   * @param sl the sequential list of methods
   * @param parallelList the list of methods that can be run in parallel
   */
  private void computeTestLists(List<List<ITestNGMethod>> sl,
                                List<ITestNGMethod> parallelList) 
  {

    Map<String, String> groupsDependedUpon= new HashMap<String, String>();
    Map<String, String> methodsDependedUpon= new HashMap<String, String>();
    
    Map<String, List<ITestNGMethod>> sequentialAttributeList = new HashMap<String, List<ITestNGMethod>>();
    List<ITestNGMethod> sequentialList = new ArrayList<ITestNGMethod>();

    for (int i= m_allTestMethods.length - 1; i >= 0; i--) {
      ITestNGMethod tm= m_allTestMethods[i];
      
      //
      // If the class this method belongs to has @Test(sequential = true), we
      // put this method in the sequential list right away
      //
      Class<?> cls= tm.getRealClass();
      org.testng.internal.annotations.ITest test = 
        (org.testng.internal.annotations.ITest) m_annotationFinder.
          findAnnotation(cls, org.testng.internal.annotations.ITest.class);
      if (test != null) {
        if (test.getSequential()) {
          String className = cls.getName();
          List<ITestNGMethod> list = sequentialAttributeList.get(className);
          if (list == null) {
            list = new ArrayList<ITestNGMethod>();
            sequentialAttributeList.put(className, list);
          }
          list.add(0, tm);
          continue;
        }
      }
      
      //
      // Otherwise, determine if it depends on other methods/groups or if
      // it is depended upon
      //
      String[] currentGroups = tm.getGroups();
      String[] currentGroupsDependedUpon= tm.getGroupsDependedUpon();
      String[] currentMethodsDependedUpon= tm.getMethodsDependedUpon();

      String thisMethodName = tm.getMethod().getDeclaringClass().getName() 
        + "." + tm.getMethod().getName();
      if (currentGroupsDependedUpon.length > 0) {
        for (String gdu : currentGroupsDependedUpon) {
          groupsDependedUpon.put(gdu, gdu);
        }

        sequentialList.add(0, tm);
      }
      else if (currentMethodsDependedUpon.length > 0) {
        for (String cmu : currentMethodsDependedUpon) {
          methodsDependedUpon.put(cmu, cmu);
        }
        sequentialList.add(0, tm);
      }
      // Is there a method that depends on the current method?
      else if (containsString(methodsDependedUpon, thisMethodName)) {
        sequentialList.add(0, tm);
      }
      else if (currentGroups.length > 0) {
        boolean isSequential= false;

        for (String group : currentGroups) {
          if (containsString(groupsDependedUpon, group)) {
            sequentialList.add(0, tm);
            isSequential = true;

            break;
          }
        }
        if (!isSequential) {
          parallelList.add(0, tm);
        }
      }
      else {
        parallelList.add(0, tm);
      }
    }
    
    //
    // Put all the sequential methods in the output argument
    //
    if(sequentialList.size() > 0) {
      sl.add(sequentialList);
    }
    
    sl.addAll(sequentialAttributeList.values());
  }

  /**
   * Logs the beginning of the {@link #beforeRun()} .
   */
  private void logStart() {
    log(3,
        "Running test " + m_testName + " on " + m_classMap.size() + " " + " classes, "
        + " included groups:[" + mapToString(m_xmlMethodSelector.getIncludedGroups())
        + "] excluded groups:[" + mapToString(m_xmlMethodSelector.getExcludedGroups()) + "]");

    if (getVerbose() >= 3) {
      for (ITestClass tc : m_classMap.values()) {
        ((TestClass) tc).dump();
      }
    }
  }

  /**
   * Trigger the start/finish event.
   *
   * @param isStart <tt>true</tt> if the event is for start, <tt>false</tt> if the
   *                event is for finish
   */
  private void fireEvent(boolean isStart) {
    for (ITestListener itl : m_testListeners) {
      if (isStart) {
        itl.onStart(this);
      }
      else {
        itl.onFinish(this);
      }
    }
  }

  /////
  // ITestContext
  //
  public String getName() {
    return m_testName;
  }

  /**
   * @return Returns the startDate.
   */
  public Date getStartDate() {
    return m_startDate;
  }

  /**
   * @return Returns the endDate.
   */
  public Date getEndDate() {
    return m_endDate;
  }

  public IResultMap getPassedTests() {
    return m_passedTests;
  }

  public IResultMap getSkippedTests() {
    return m_skippedTests;
  }

  public IResultMap getFailedTests() {
    return m_failedTests;
  }

  public IResultMap getFailedButWithinSuccessPercentageTests() {
    return m_failedButWithinSuccessPercentageTests;
  }

  public String[] getIncludedGroups() {
    Map<String, String> ig= m_xmlMethodSelector.getIncludedGroups();
    String[] result= (String[]) ig.values().toArray((new String[ig.size()]));

    return result;
  }

  public String[] getExcludedGroups() {
    Map<String, String> eg= m_xmlMethodSelector.getExcludedGroups();
    String[] result= (String[]) eg.values().toArray((new String[eg.size()]));

    return result;
  }

  public String getOutputDirectory() {
    return m_outputDirectory;
  }

  /**
   * @return Returns the suite.
   */
  public ISuite getSuite() {
    return m_suite;
  }

  public ITestNGMethod[] getAllTestMethods() {
    return m_allTestMethods;
  }

  
  public String getHost() {
    return m_host;
  }

  public Collection<ITestNGMethod> getExcludedMethods() {
    Map<ITestNGMethod, ITestNGMethod> vResult = 
      new HashMap<ITestNGMethod, ITestNGMethod>();
    
    for (ITestNGMethod m : m_excludedMethods) {
      vResult.put(m, m);
    }
    
    return vResult.keySet();
  }

  /**
   * @see org.testng.ITestContext#getFailedConfigurations()
   */
  public IResultMap getFailedConfigurations() {
    return m_failedConfigurations;
  }

  /**
   * @see org.testng.ITestContext#getPassedConfigurations()
   */
  public IResultMap getPassedConfigurations() {
    return m_passedConfigurations;
  }

  /**
   * @see org.testng.ITestContext#getSkippedConfigurations()
   */
  public IResultMap getSkippedConfigurations() {
    return m_skippedConfigurations;
  }
  
  //
  // ITestContext
  /////
  
  /////
  // ITestResultNotifier
  //

  public void addPassedTest(ITestNGMethod tm, ITestResult tr) {
    m_passedTests.addResult(tr, tm);
  }

  public Set<ITestResult> getPassedTests(ITestNGMethod tm) {
    return m_passedTests.getResults(tm);
  }

  public void addSkippedTest(ITestNGMethod tm, ITestResult tr) {
    m_skippedTests.addResult(tr, tm);
  }

  public void addInvokedMethod(InvokedMethod im) {
    synchronized(m_invokedMethods) {
      m_invokedMethods.add(im);
    }
  }

  public void addFailedTest(ITestNGMethod testMethod, ITestResult result) {
    logFailedTest(testMethod, result, false /* withinSuccessPercentage */);
  }

  public void addFailedButWithinSuccessPercentageTest(ITestNGMethod testMethod,
                                                      ITestResult result) {
    logFailedTest(testMethod, result, true /* withinSuccessPercentage */);
  }

  public XmlTest getTest() {
    return m_xmlTest;
  }

  public List<ITestListener> getTestListeners() {
    return m_testListeners;
  }

  public List<IConfigurationListener> getConfigurationListeners() {
    return m_configurationListeners;
  }
  //
  // ITestResultNotifier
  /////

  private void logFailedTest(ITestNGMethod method,
                             ITestResult tr,
                             boolean withinSuccessPercentage) {
    if (withinSuccessPercentage) {
      m_failedButWithinSuccessPercentageTests.addResult(tr, method);
    }
    else {
      m_failedTests.addResult(tr, method);
    }
  }

  private String mapToString(Map<?, ?> m) {
    StringBuffer result= new StringBuffer();
    for (Object o : m.values()) {
      result.append(o.toString()).append(" ");
    }

    return result.toString();
  }

  private void log(int level, String s) {
    Utils.log("TestRunner", level, s);
  }

  public static int getVerbose() {
    return m_verbose;
  }

  public void setVerbose(int n) {
    m_verbose = n;
  }

  private void log(String s) {
    Utils.log("TestRunner", 2, s);
  }

  /////
  // Listeners
  //
  public void addListener(Object listener) {
    if(listener instanceof ITestListener) {
      addTestListener((ITestListener) listener);
    }
    if(listener instanceof IConfigurationListener) {
      addConfigurationListener((IConfigurationListener) listener);
    }
  }
  
  public void addTestListener(ITestListener il) {
    m_testListeners.add(il);
  }

  public void addConfigurationListener(IConfigurationListener icl) {
    m_configurationListeners.add(icl);
  }
  //
  // Listeners
  /////

  private List<InvokedMethod> m_invokedMethods = new ArrayList<InvokedMethod>();

  private void dumpInvokedMethods() {
    System.out.println("\n*********** INVOKED METHODS\n");
    for (InvokedMethod im : m_invokedMethods) {
      if (im.isTestMethod()) {
        System.out.print("\t\t");
      }
      else if (im.isConfigurationMethod()) {
        System.out.print("\t");
      }
      else {
        continue;
      }
      System.out.println("" + im);
    }
    System.out.println("\n***********\n");
  }

  /**
   * @return
   */
  public List<ITestNGMethod> getInvokedMethods() {
    List<ITestNGMethod> result= new ArrayList<ITestNGMethod>();
    for (InvokedMethod im : m_invokedMethods) {
      ITestNGMethod tm= im.getTestMethod();
      tm.setDate(im.getDate());
      result.add(tm);
    }

    return result;
  }

  private IResultMap m_passedConfigurations= new ResultMap();
  private IResultMap m_skippedConfigurations= new ResultMap();
  private IResultMap m_failedConfigurations= new ResultMap();
  
  private class ConfigurationListener implements IConfigurationListener {
    public void onConfigurationFailure(ITestResult itr) {
      m_failedConfigurations.addResult(itr, itr.getMethod());
    }

    public void onConfigurationSkip(ITestResult itr) {
      m_skippedConfigurations.addResult(itr, itr.getMethod());
    }

    public void onConfigurationSuccess(ITestResult itr) {
      m_passedConfigurations.addResult(itr, itr.getMethod());
    }
  }

  public void setMethodInterceptor(IMethodInterceptor methodInterceptor) {
    m_methodInterceptor = methodInterceptor;
  }
} // TestRunner
