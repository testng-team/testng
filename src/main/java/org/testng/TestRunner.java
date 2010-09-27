package org.testng;

import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.Attributes;
import org.testng.internal.ClassHelper;
import org.testng.internal.ClassInfoMap;
import org.testng.internal.ConfigurationGroupMethods;
import org.testng.internal.Constants;
import org.testng.internal.DynamicGraph;
import org.testng.internal.IConfiguration;
import org.testng.internal.IConfigurationListener;
import org.testng.internal.IInvoker;
import org.testng.internal.ITestResultNotifier;
import org.testng.internal.InvokedMethod;
import org.testng.internal.Invoker;
import org.testng.internal.MapList;
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
import org.testng.internal.annotations.IListeners;
import org.testng.internal.annotations.Sets;
import org.testng.internal.thread.ThreadUtil;
import org.testng.internal.thread.graph.GraphThreadPoolExecutor;
import org.testng.internal.thread.graph.IThreadWorkerFactory;
import org.testng.internal.thread.graph.IWorker;
import org.testng.junit.IJUnitTestRunner;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * This class takes care of running one Test.
 *
 * @author Cedric Beust, Apr 26, 2004
 */
public class TestRunner implements ITestContext, ITestResultNotifier, IThreadWorkerFactory<ITestNGMethod> {
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
  transient private List<ITestListener> m_testListeners = Lists.newArrayList();
  transient private List<IConfigurationListener> m_configurationListeners = Lists.newArrayList();

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
  transient private Map<Class<?>, ITestClass> m_classMap = Maps.newHashMap();

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
  private List<ITestNGMethod> m_excludedMethods = Lists.newArrayList();
  private ConfigurationGroupMethods m_groupMethods = null;

  // Meta groups
  private Map<String, List<String>> m_metaGroups = Maps.newHashMap();

  // All the tests that were run along with their result
  private IResultMap m_passedTests = new ResultMap();
  private IResultMap m_failedTests = new ResultMap();
  private IResultMap m_failedButWithinSuccessPercentageTests = new ResultMap();
  private IResultMap m_skippedTests = new ResultMap();

  private RunInfo m_runInfo= new RunInfo();
  
  // The host where this test was run, or null if run locally
  private String m_host;

  // Defined dynamically depending on <test preserve-order="true/false">
  private transient IMethodInterceptor m_methodInterceptor;

  private transient ClassMethodMap m_classMethodMap;
  private transient TestNGClassFinder m_testClassFinder;
  private transient IConfiguration m_configuration;

  public TestRunner(IConfiguration configuration,
                    ISuite suite,
                    XmlTest test,
                    String outputDirectory,
                    IAnnotationFinder finder,
                    boolean skipFailedInvocationCounts,
                    List<IInvokedMethodListener> invokedMethodListeners) 
  {
    init(configuration, suite, test, outputDirectory, finder, skipFailedInvocationCounts,
        invokedMethodListeners);
  }

  public TestRunner(IConfiguration configuration, ISuite suite, XmlTest test, 
    IAnnotationFinder finder, boolean skipFailedInvocationCounts) 
  {
    init(configuration, suite, test, suite.getOutputDirectory(), finder, skipFailedInvocationCounts,
        null);
  }

  public TestRunner(IConfiguration configuration, ISuite suite, XmlTest test,
      boolean skipFailedInvocationCounts,
      List<IInvokedMethodListener> listeners) {
    init(configuration, suite, test, suite.getOutputDirectory(), 
        suite.getAnnotationFinder(test.getAnnotations()),
        skipFailedInvocationCounts, listeners);
  }

  private void init(IConfiguration configuration,
                    ISuite suite,
                    XmlTest test,
                    String outputDirectory,
                    IAnnotationFinder annotationFinder,
                    boolean skipFailedInvocationCounts,
                    List<IInvokedMethodListener> invokedMethodListeners)
  {
    m_configuration = configuration;
    m_xmlTest= test;
    m_suite = suite;
    m_testName = test.getName();
    m_host = suite.getHost();
    m_testClassesFromXml= test.getXmlClasses();
    m_skipFailedInvocationCounts = skipFailedInvocationCounts;
    setVerbose(test.getVerbose());

    boolean preserveOrder = "true".equalsIgnoreCase(test.getPreserveOrder());
    m_methodInterceptor = preserveOrder ? new PreserveOrderMethodInterceptor()
        : new InstanceOrderingMethodInterceptor();

    m_packageNamesFromXml= test.getXmlPackages();    
    if(null != m_packageNamesFromXml) {
      for(XmlPackage xp: m_packageNamesFromXml) {
        m_testClassesFromXml.addAll(xp.getXmlClasses());
      }
    }

    m_annotationFinder= annotationFinder;
    m_invoker = new Invoker(m_configuration, this, this, m_suite.getSuiteState(),
        m_skipFailedInvocationCounts, invokedMethodListeners);

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

  private void init() {
    initMetaGroups(m_xmlTest);
    initRunInfo(m_xmlTest);

    // Init methods and class map
    // JUnit behavior is different and doesn't need this initialization step
    if(!m_xmlTest.isJUnit()) {
      initMethods();
    }

    initListeners();
    addConfigurationListener(m_confListener);
  }

  class ListenerHolder {
    List<Class<? extends ITestNGListener>> listenerClasses;
    Class<? extends ITestNGListenerFactory> listenerFactoryClass;
  }

  /**
   * @return all the @Listeners annotations found in the current class and its
   * superclasses.
   */
  private ListenerHolder findAllListeners(Class<?> cls) {
    ListenerHolder result = new ListenerHolder();
    result.listenerClasses = Lists.newArrayList();

    do {
      IListeners l = (IListeners) m_annotationFinder.findAnnotation(cls, IListeners.class);
      if (l != null) {
        Class<? extends ITestNGListener>[] classes = l.getValue();
        for (Class<? extends ITestNGListener> c : classes) {
          result.listenerClasses.add(c);

          if (ITestNGListenerFactory.class.isAssignableFrom(c)) {
            if (result.listenerFactoryClass == null) {
              result.listenerFactoryClass = (Class<? extends ITestNGListenerFactory>) c;
            }
            else {
              throw new TestNGException("Found more than one class implementing" +
                  "ITestNGListenerFactory:" + c + " and " + result.listenerFactoryClass);
            }
          }
        }
      }
      cls = cls.getSuperclass();
    } while (cls != Object.class);

    return result;
  }

  private void initListeners() {
    //
    // Find all the listener factories and collect all the listeners requested in a
    // @Listeners annotation.
    //
    List<Class<? extends ITestNGListener>> listenerClasses = Lists.newArrayList();
    Class<? extends ITestNGListenerFactory> listenerFactoryClass = null;

    for (IClass cls : getTestClasses()) {
      Class<? extends ITestNGListenerFactory> realClass = cls.getRealClass();
      ListenerHolder listenerHolder = findAllListeners(realClass);
      if (listenerFactoryClass == null) listenerFactoryClass = listenerHolder.listenerFactoryClass;
      listenerClasses.addAll(listenerHolder.listenerClasses);
    }

    //
    // Now we have all the listeners collected from @Listeners and at most one
    // listener factory collected from a class implementing ITestNGListenerFactory.
    // Instantiate all the requested listeners.
    //
    ITestNGListenerFactory listenerFactory = null;
 
    // If we found a test listener factory, instantiate it.
    try {
      if (m_testClassFinder != null) {
        IClass ic = m_testClassFinder.getIClass(listenerFactoryClass);
        if (ic != null) {
          listenerFactory = (ITestNGListenerFactory) ic.getInstances(false)[0];
        }
      }
      if (listenerFactory == null) {
        listenerFactory = listenerFactoryClass != null ? listenerFactoryClass.newInstance() : null;
      }
    }
    catch(Exception ex) {
      throw new TestNGException("Couldn't instantiate the ITestNGListenerFactory: "
          + ex);
    }

    // Instantiate all the listeners
    for (Class<? extends ITestNGListener> c : listenerClasses) {
      Object listener = listenerFactory != null ? listenerFactory.createListener(c) : null;
      if (listener == null) listener = ClassHelper.newInstance(c);

      if (listener instanceof IMethodInterceptor) {
        setMethodInterceptor((IMethodInterceptor) listener);
      }
      if (listener instanceof ISuiteListener) {
        addListener(listener);
      }
      if (listener instanceof IInvokedMethodListener) {
        m_suite.addListener((ITestNGListener) listener);
      }
      if (listener instanceof ITestListener) {
        // At this point, the field m_testListeners has already been used in the creation
        addTestListener((ITestListener) listener);
      }
      if (listener instanceof IReporter) {
        m_suite.addListener((ITestNGListener) listener);
      }
      if (listener instanceof IConfigurable) {
        m_configuration.setConfigurable((IConfigurable) listener);
      }
      if (listener instanceof IHookable) {
        m_configuration.setHookable((IHookable) listener);
      }
    }
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
    List<ITestNGMethod> beforeClassMethods = Lists.newArrayList();
    List<ITestNGMethod> testMethods = Lists.newArrayList();
    List<ITestNGMethod> afterClassMethods = Lists.newArrayList();
    List<ITestNGMethod> beforeSuiteMethods = Lists.newArrayList();
    List<ITestNGMethod> afterSuiteMethods = Lists.newArrayList();
    List<ITestNGMethod> beforeXmlTestMethods = Lists.newArrayList();
    List<ITestNGMethod> afterXmlTestMethods = Lists.newArrayList();

    ClassInfoMap classMap = new ClassInfoMap(m_testClassesFromXml);
    m_testClassFinder= new TestNGClassFinder(classMap,
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
    IClass[] classes = m_testClassFinder.findTestClasses();
    
    for (IClass ic : classes) {

      // Create TestClass
      ITestClass tc = new TestClass(ic,
                                   testMethodFinder,
                                   m_annotationFinder,
                                   m_runInfo,
                                   m_xmlTest,
                                   classMap.getXmlClass(ic.getRealClass()));
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
      fixMethodsWithClass(tc.getTestMethods(), tc, testMethods);
      fixMethodsWithClass(tc.getBeforeClassMethods(), tc, beforeClassMethods);
      fixMethodsWithClass(tc.getBeforeTestMethods(), tc, null);
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
    m_classMethodMap = new ClassMethodMap(m_allTestMethods);

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

  public Collection<ITestClass> getTestClasses() {
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
    return createGroups(groups.toArray(new String[groups.size()]));
  }

  private Map<String, String> createGroups(String[] groups) {
    Map<String, String> result = Maps.newHashMap();

    // Groups that were passed on the command line
    for (String group : groups) {
      result.put(group, group);
    }

    // See if we have any MetaGroups and
    // expand them if they match one of the groups
    // we have just been passed
    List<String> unfinishedGroups = Lists.newArrayList();

    if (m_metaGroups.size() > 0) {
      collectGroups(groups, unfinishedGroups, result);

      // Do we need to loop over unfinished groups?
      while (unfinishedGroups.size() > 0) {
        String[] uGroups = unfinishedGroups.toArray(new String[unfinishedGroups.size()]);
        unfinishedGroups = Lists.newArrayList();
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
                                     null, /* no parameter values */
                                     null /* instance */);
    }
  }

  private void privateRunJUnit(XmlTest xmlTest) {
    ClassInfoMap cim = new ClassInfoMap(m_testClassesFromXml);
    final Set<Class<?>> classes = cim.getClasses();
    final List<ITestNGMethod> runMethods = Lists.newArrayList();
    List<IWorker<ITestNGMethod>> workers = Lists.newArrayList();
    // FIXME: directly referencing JUnitTestRunner which uses JUnit classes
    // may result in an class resolution exception under different JVMs
    // The resolution process is not specified in the JVM spec with a specific implementation,
    // so it can be eager => failure
    workers.add(new IWorker<ITestNGMethod>() {
      /**
       * @see org.testng.internal.IMethodWorker#getMaxTimeOut()
       */
      @Override
      public long getTimeOut() {
        return 0;
      }

      /**
       * @see java.lang.Runnable#run()
       */
      @Override
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

      @Override
      public List<ITestNGMethod> getTasks() {
        throw new TestNGException("JUnit not supported");
      }

      @Override
      public int getPriority() {
        if (m_allTestMethods.length == 1) return m_allTestMethods[0].getPriority();
        else return 0;
      }

      @Override
      public int compareTo(IWorker<ITestNGMethod> other) {
        return getPriority() - other.getPriority();
      }
    });

    runWorkers(workers, "" /* JUnit does not support parallel */, null);
    m_allTestMethods= runMethods.toArray(new ITestNGMethod[runMethods.size()]);
  }
  
  public void privateRun(XmlTest xmlTest) {
    //
    // Calculate the lists of tests that can be run in sequence and in parallel
    //
    List<List<ITestNGMethod>> sequentialList= Lists.newArrayList();
    List<ITestNGMethod> parallelList= Lists.newArrayList();
    MapList<Integer, ITestNGMethod> sequentialMapList = new MapList<Integer, ITestNGMethod>();

    String parallelMode = xmlTest.getParallel();
    boolean parallel = XmlSuite.PARALLEL_METHODS.equals(parallelMode) 
        || "true".equalsIgnoreCase(parallelMode)
        || XmlSuite.PARALLEL_CLASSES.equals(parallelMode); 

    if (!parallel) {
      // sequential
      computeTestLists(sequentialList, parallelList, sequentialMapList);
      
      log(3, "Found " + (sequentialList.size() + parallelList.size()) + " applicable methods");

      //
      // If the user specified preserve-order = true, we can't change the ordering
      // of the methods on the sequential list, since they are not free, however
      // we can still reorder the classes to reflect that order.
      //
      if ("true".equalsIgnoreCase(xmlTest.getPreserveOrder())) {
        // Note: modifying sequentialList
        sequentialList = preserveClassOrder(xmlTest, sequentialList);
      }

      //
      // Create the workers
      //
      List<TestMethodWorker> workers = Lists.newArrayList();
  
      createSequentialWorkers(sequentialList, xmlTest.getParameters(), m_classMethodMap, workers);
      MapList<Integer, TestMethodWorker> ml =
          createSequentialWorkers(sequentialMapList, xmlTest.getParameters(), m_classMethodMap);
  
      // All the parallel tests are placed in a separate worker, so they can be
      // invoked in parallel
      createParallelWorkers(parallelList, xmlTest, m_classMethodMap, workers);

//      m_testPlan =
//        new TestPlan(sequentialList, parallelList, cmm,
//          getBeforeSuiteMethods(), getAfterSuiteMethods(),
//          m_groupMethods, xmlTest);
  
      try {
        // Sort by priorities
        Collections.sort(workers);
        runWorkers(workers, xmlTest.getParallel(), ml);
      }
      finally {
        m_classMethodMap.clear();
      }
    }
    else {
      // parallel
      int threadCount = xmlTest.getThreadCount();
      DynamicGraph<ITestNGMethod> graph = computeAlternateTestList(m_allTestMethods);
      if (graph.getNodeCount() > 0) {
        GraphThreadPoolExecutor<ITestNGMethod> executor = new GraphThreadPoolExecutor<ITestNGMethod>(graph, this,
            threadCount, threadCount, 0, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
        executor.run();
        try {
          long timeOut = m_xmlTest.getTimeOut(XmlTest.DEFAULT_TIMEOUT_MS);
          Utils.log("TestRunner", 2, "Starting executor for test " + m_xmlTest.getName()
              + " with time out:" + timeOut + " milliseconds.");
          executor.awaitTermination(timeOut, TimeUnit.MILLISECONDS);
          executor.shutdownNow();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Reorder the methods to preserve the class order without changing the method ordering.
   */
  private List<List<ITestNGMethod>> preserveClassOrder(XmlTest test,
      List<List<ITestNGMethod>> lists) {

    List<List<ITestNGMethod>> result = Lists.newArrayList();

    Map<String, List<ITestNGMethod>> classes = Maps.newHashMap();
    List<XmlClass> sortedClasses = Lists.newArrayList();

    for (XmlClass c : test.getXmlClasses()) {
      classes.put(c.getName(), new ArrayList<ITestNGMethod>());
      sortedClasses.add(c);
    }

    // Sort the classes based on their order of appearance in the XML
    Collections.sort(sortedClasses, new Comparator<XmlClass>() {
      @Override
      public int compare(XmlClass arg0, XmlClass arg1) {
        return arg0.getIndex() - arg1.getIndex();
      }
    });

    // Put each method in their class bucket
    for (List<ITestNGMethod> ll : lists) {
      for (ITestNGMethod m : ll) {
        List<ITestNGMethod> l = classes.get(m.getMethod().getDeclaringClass().getName());
        l.add(m);
      }
    }
      // Recreate the list based on the class ordering
    List<ITestNGMethod> tmpResult = Lists.newArrayList();
      for (XmlClass xc : sortedClasses) {
        List<ITestNGMethod> methods = classes.get(xc.getName());
        tmpResult.addAll(methods);
      }
      result.add(tmpResult);
//    }

//    System.out.println(result);
    return result;
  }

  /**
   * Create a list of workers to run the methods passed in parameter.
   * Each test method is run in its own worker except in the following cases:
   * - The method belongs to a class that has @Test(sequential=true)
   * - The parallel attribute is set to "classes"
   * In both these cases, all the methods belonging to that class will then
   * be put in the same worker in order to run in the same thread.
   */
  @Override
  public List<IWorker<ITestNGMethod>> createWorkers(Set<ITestNGMethod> methods) {
    List<IWorker<ITestNGMethod>> result = Lists.newArrayList();

    // Methods that belong to classes with a sequential=true or parallel=classes
    // attribute must all be run in the same worker
    Set<Class> sequentialClasses = Sets.newHashSet();
    for (ITestNGMethod m : methods) {
      Class<? extends ITestClass> cls = m.getRealClass();
      org.testng.annotations.ITestAnnotation test = 
        (org.testng.annotations.ITestAnnotation) m_annotationFinder.
          findAnnotation(cls,
              org.testng.annotations.ITestAnnotation.class);

      // If either sequential=true or parallel=classes, mark this class sequential
      if (test != null && (test.getSequential() || test.getSingleThreaded()) ||
          XmlSuite.PARALLEL_CLASSES.equals(m_xmlTest.getParallel())) {
        sequentialClasses.add(cls);
      }
    }

    List<IMethodInstance> methodInstances = Lists.newArrayList();
    for (ITestNGMethod tm : methods) {
      methodInstances.addAll(methodsToMultipleMethodInstances(tm));
    }

    //
    // Finally, sort the parallel methods by classes
    //
    methodInstances = m_methodInterceptor.intercept(methodInstances, this);
    Map<String, String> params = m_xmlTest.getParameters();

    Set<Class<?>> processedClasses = Sets.newHashSet();
    for (IMethodInstance im : methodInstances) {
      Class<?> c = im.getMethod().getTestClass().getRealClass();
      if (sequentialClasses.contains(c)) {
        if (!processedClasses.contains(c)) {
          processedClasses.add(c);
          if (System.getProperty("experimental") != null) {
            List<IMethodInstance>[] instances = createInstances(methodInstances);
            for (List<IMethodInstance> inst : instances) {
              TestMethodWorker worker = createTestMethodWorker(inst, params, c);
              result.add(worker);
            }
          }
          else {
            // Sequential class: all methods in one worker
            TestMethodWorker worker = createTestMethodWorker(methodInstances, params, c);
            result.add(worker);
          }
        }
      }
      else {
        // Parallel class: each method in its own worker
          TestMethodWorker worker = createTestMethodWorker(Arrays.asList(im), params, c);
          result.add(worker);
      }
    }

    // Sort by priorities
    Collections.sort(result);
    return result;
  }

  private List<IMethodInstance>[] createInstances(List<IMethodInstance> methodInstances) {
    Map<Object, List<IMethodInstance>> map = Maps.newHashMap();
//    MapList<IMethodInstance[], Object> map = new MapList<IMethodInstance[], Object>();
    for (IMethodInstance imi : methodInstances) {
      for (Object o : imi.getInstances()) {
        System.out.println(o);
        List<IMethodInstance> l = map.get(o);
        if (l == null) {
          l = Lists.newArrayList();
          map.put(o, l);
        }
        l.add(imi);
      }
//      for (Object instance : imi.getInstances()) {
//        map.put(imi, instance);
//      }
    }
//    return map.getKeys();
    System.out.println(map);
    List[] result = new List[map.size()];
    int i = 0;
    for (List<IMethodInstance> imi : map.values()) {
      result[i++] = imi;
    }
    return result;
  }

  private TestMethodWorker createTestMethodWorker(
      List<IMethodInstance> methodInstances, Map<String, String> params,
      Class<?> c) {
    return new TestMethodWorker(m_invoker,
        findClasses(methodInstances, c),
        m_xmlTest.getSuite(),
        params,
        m_allTestMethods,
        m_groupMethods,
        m_classMethodMap,
        this);
  }

  private IMethodInstance[] findClasses(List<IMethodInstance> methodInstances, Class<?> c) {
    List<IMethodInstance> result = Lists.newArrayList();
    for (IMethodInstance mi : methodInstances) {
      if (mi.getMethod().getTestClass().getRealClass() == c) {
        result.add(mi);
      }
    }
    return result.toArray(new IMethodInstance[result.size()]);
  }

  private void createParallelWorkers(List<ITestNGMethod> parallel, 
      XmlTest xmlTest, ClassMethodMap cmm, List<TestMethodWorker> workers) {

    if(parallel.isEmpty()) return;
    
    List<IMethodInstance> methodInstances = Lists.newArrayList();
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

    Map<String, String> params = xmlTest.getParameters();
    // This should no longer happen when we are running the new 5.11 implementation but keeping
    // it until I'm sure the new implementation is working fine.
    // @deprecated
    if (XmlSuite.PARALLEL_CLASSES.equals(xmlTest.getParallel())) {
      Map<Class, Set<IMethodInstance>> list = groupMethodInstancesByClass(methodInstances);
      for (Set<IMethodInstance> s : list.values()) {
          workers.add(new TestMethodWorker(m_invoker,
              s.toArray(new IMethodInstance[s.size()]),
              m_xmlTest.getSuite(),
              params,
              m_allTestMethods,
              m_groupMethods,
              cmm,
              this));          
      }
    }
    else {
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

  }

  /**
   * @return a Set of arrays of IMethodInstances. Each element in the array is a method that belongs
   * to the same class.
   */
  private Map<Class, Set<IMethodInstance>> groupMethodInstancesByClass(List<IMethodInstance> instances) {
      Map<Class, Set<IMethodInstance>> result = Maps.newHashMap();
      for (IMethodInstance mi : instances) {
          Class cl = mi.getMethod().getTestClass().getRealClass();
          Set<IMethodInstance> methods = result.get(cl);
          if (methods == null) {
              methods = new HashSet<IMethodInstance>();
              result.put(cl, methods);
          }
          methods.add(mi);
      }

      return result;
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
      log(3, "Will be run sequentially:");
      for (List<ITestNGMethod> l : sequentialList) {
        for (ITestNGMethod tm : l) {
          log(3, "  " + tm);
        }
        log(3, "====");
      }
      
      log(3, "===");
    }
  }

  private MapList<Integer, TestMethodWorker> createSequentialWorkers(MapList<Integer,
      ITestNGMethod> mapList, Map<String, String> params, ClassMethodMap cmm) { 

    MapList<Integer, TestMethodWorker> result = new MapList<Integer, TestMethodWorker>();
    // All the sequential tests are place in one worker, guaranteeing they
    // will be invoked sequentially
    for (Integer i : mapList.getKeys()) {
      result.put(i,
          new TestMethodWorker(m_invoker, methodsToMethodInstances(mapList.get(i)),
          m_xmlTest.getSuite(), params, m_allTestMethods, m_groupMethods, cmm, this));
    }

    if (getVerbose() >= 2) {
      log(3, "Will be run sequentially:" + result);
    }

    return result;
  }

  private List<MethodInstance> methodsToMultipleMethodInstances(ITestNGMethod... sl) {
    List<MethodInstance> vResult = Lists.newArrayList();
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
  private void runWorkers(List<? extends IWorker<ITestNGMethod>> workers, String parallelMode,
      MapList<Integer, TestMethodWorker> sequentialWorkers) {
    if (XmlSuite.PARALLEL_METHODS.equals(parallelMode) 
        || "true".equalsIgnoreCase(parallelMode)
        || XmlSuite.PARALLEL_CLASSES.equals(parallelMode)) 
    {
      //
      // Parallel run
      //
      // Default timeout for individual methods:  same as the test global-time-out, but
      // overridden if a method defines its own.
      long maxTimeOut = m_xmlTest.getTimeOut(XmlTest.DEFAULT_TIMEOUT_MS);
      for (IWorker<ITestNGMethod> tmw : workers) {
        long mt = tmw.getTimeOut();
        if (mt > maxTimeOut) {
          maxTimeOut= mt;
        }
      }

      ThreadUtil.execute(workers, m_xmlTest.getThreadCount(), maxTimeOut, false);
//      ThreadUtil.execute(sequentialWorkers);
    }
    else {
      //
      // Sequential run
      //
      for (IWorker<ITestNGMethod> tmw : workers) {
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
                                     null, /* no parameter values */
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

  private DynamicGraph<ITestNGMethod> computeAlternateTestList(ITestNGMethod[] methods) {
    DynamicGraph<ITestNGMethod> result = new DynamicGraph<ITestNGMethod>();
    Map<String, ITestNGMethod> map = Maps.newHashMap();
    MapList<String, ITestNGMethod> groups = new MapList<String, ITestNGMethod>();

    for (ITestNGMethod m : methods) {
      map.put(m.getTestClass().getName() + "." + m.getMethodName(), m);
      for (String g : m.getGroups()) {
        groups.put(g, m);
      }
    }
    
    for (ITestNGMethod m : methods) {
      result.addNode(m);
      
      // Dependent methods
      {
        String[] dependentMethods = m.getMethodsDependedUpon();
        if (dependentMethods != null) {
          for (String d : dependentMethods) {
            ITestNGMethod dm = map.get(d);
            if (dm == null) {
              throw new TestNGException("Method \"" + m
                  + "\" depends on nonexistent method \"" + d + "\""); 
            }
            result.addEdge(m, dm);
          }
        }
      }

      // Dependent groups
      {
        String[] dependentGroups = m.getGroupsDependedUpon();
        for (String d : dependentGroups) {
          List<ITestNGMethod> dg = groups.get(d);
          if (dg == null) {
            throw new TestNGException("Method \"" + m
                + "\" depends on nonexistent group \"" + d + "\""); 
          }
          for (ITestNGMethod ddm : dg) {
            result.addEdge(m, ddm);
          }
        }
      }
    }

    return result;
  }

  /**
   * Creates the
   * @param sl the sequential list of methods
   * @param parallelList the list of methods that can be run in parallel
   */
  private void computeTestLists(List<List<ITestNGMethod>> sl,
      List<ITestNGMethod> parallelList, MapList<Integer, ITestNGMethod> outSequentialList) {

    Map<String, String> groupsDependedUpon = Maps.newHashMap();
    Map<String, String> methodsDependedUpon = Maps.newHashMap();
    
    Map<String, List<ITestNGMethod>> sequentialAttributeList = Maps.newHashMap();
    List<ITestNGMethod> sequentialList = Lists.newArrayList();

    for (int i= m_allTestMethods.length - 1; i >= 0; i--) {
      ITestNGMethod tm= m_allTestMethods[i];
      
      //
      // If the class this method belongs to has @Test(sequential = true), we
      // put this method in the sequential list right away
      //
      Class<?> cls= tm.getRealClass();
      org.testng.annotations.ITestAnnotation test = 
        (org.testng.annotations.ITestAnnotation) m_annotationFinder.
          findAnnotation(cls, org.testng.annotations.ITestAnnotation.class);
      if (test != null) {
        if (test.getSequential() || test.getSingleThreaded()) {
          String className = tm.getTestClass().getName();
          List<ITestNGMethod> list = sequentialAttributeList.get(className);
          if (list == null) {
            list = Lists.newArrayList();
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
        int index = 0;
        for (int j = 0; j < sequentialList.size(); j++) {
          ITestNGMethod m = sequentialList.get(j);
          if (arrayContains(m.getMethodsDependedUpon(), thisMethodName)) {
            index = j;
            break;
          }
        }
        // Insert the dependee as close to its dependent as possible (TESTNG-317)
        sequentialList.add(index, tm);
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

    String previousGroup = "";
    int index = 0;
    for (ITestNGMethod m : sequentialList) {
      String[] g = m.getGroupsDependedUpon();
      if (g.length > 0 && !m.getGroupsDependedUpon()[0].equals(previousGroup)) {
        index++;
        previousGroup = m.getGroupsDependedUpon()[0];
      }
      outSequentialList.put(index, m);
    }
//    System.out.println("Map list:" + mapList);

    sl.addAll(sequentialAttributeList.values());
  }

  private boolean arrayContains(String[] array, String element) {
    for (String a : array) {
      if (element.equals(a)) return true;
    }
    return false;
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
  @Override
  public String getName() {
    return m_testName;
  }

  /**
   * @return Returns the startDate.
   */
  @Override
  public Date getStartDate() {
    return m_startDate;
  }

  /**
   * @return Returns the endDate.
   */
  @Override
  public Date getEndDate() {
    return m_endDate;
  }

  @Override
  public IResultMap getPassedTests() {
    return m_passedTests;
  }

  @Override
  public IResultMap getSkippedTests() {
    return m_skippedTests;
  }

  @Override
  public IResultMap getFailedTests() {
    return m_failedTests;
  }

  @Override
  public IResultMap getFailedButWithinSuccessPercentageTests() {
    return m_failedButWithinSuccessPercentageTests;
  }

  @Override
  public String[] getIncludedGroups() {
    Map<String, String> ig= m_xmlMethodSelector.getIncludedGroups();
    String[] result= ig.values().toArray((new String[ig.size()]));

    return result;
  }

  @Override
  public String[] getExcludedGroups() {
    Map<String, String> eg= m_xmlMethodSelector.getExcludedGroups();
    String[] result= eg.values().toArray((new String[eg.size()]));

    return result;
  }

  @Override
  public String getOutputDirectory() {
    return m_outputDirectory;
  }

  /**
   * @return Returns the suite.
   */
  @Override
  public ISuite getSuite() {
    return m_suite;
  }

  @Override
  public ITestNGMethod[] getAllTestMethods() {
    return m_allTestMethods;
  }

  
  @Override
  public String getHost() {
    return m_host;
  }

  @Override
  public Collection<ITestNGMethod> getExcludedMethods() {
    Map<ITestNGMethod, ITestNGMethod> vResult = Maps.newHashMap();
    
    for (ITestNGMethod m : m_excludedMethods) {
      vResult.put(m, m);
    }
    
    return vResult.keySet();
  }

  /**
   * @see org.testng.ITestContext#getFailedConfigurations()
   */
  @Override
  public IResultMap getFailedConfigurations() {
    return m_failedConfigurations;
  }

  /**
   * @see org.testng.ITestContext#getPassedConfigurations()
   */
  @Override
  public IResultMap getPassedConfigurations() {
    return m_passedConfigurations;
  }

  /**
   * @see org.testng.ITestContext#getSkippedConfigurations()
   */
  @Override
  public IResultMap getSkippedConfigurations() {
    return m_skippedConfigurations;
  }
  
  //
  // ITestContext
  /////
  
  /////
  // ITestResultNotifier
  //

  @Override
  public void addPassedTest(ITestNGMethod tm, ITestResult tr) {
    m_passedTests.addResult(tr, tm);
  }

  @Override
  public Set<ITestResult> getPassedTests(ITestNGMethod tm) {
    return m_passedTests.getResults(tm);
  }
  
  @Override
  public Set<ITestResult> getFailedTests(ITestNGMethod tm) {
    return m_failedTests.getResults(tm);
  }

  @Override
  public void addSkippedTest(ITestNGMethod tm, ITestResult tr) {
    m_skippedTests.addResult(tr, tm);
  }

  @Override
  public void addInvokedMethod(InvokedMethod im) {
    synchronized(m_invokedMethods) {
      m_invokedMethods.add(im);
    }
  }

  @Override
  public void addFailedTest(ITestNGMethod testMethod, ITestResult result) {
    logFailedTest(testMethod, result, false /* withinSuccessPercentage */);
  }

  @Override
  public void addFailedButWithinSuccessPercentageTest(ITestNGMethod testMethod,
                                                      ITestResult result) {
    logFailedTest(testMethod, result, true /* withinSuccessPercentage */);
  }

  @Override
  public XmlTest getTest() {
    return m_xmlTest;
  }

  @Override
  public List<ITestListener> getTestListeners() {
    return m_testListeners;
  }

  @Override
  public List<IConfigurationListener> getConfigurationListeners() {
    return m_configurationListeners;
  }
  //
  // ITestResultNotifier
  /////

  private void logFailedTest(ITestNGMethod method,
                             ITestResult tr,
                             boolean withinSuccessPercentage) {
    /*
     * We should not remove a passed method from m_passedTests so that we can
     * account for the passed instances of this test method.
     */
    //m_passedTests.removeResult(method);
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

  private List<InvokedMethod> m_invokedMethods = Lists.newArrayList();

  private void dumpInvokedMethods() {
    System.out.println("\n*********** INVOKED METHODS\n");
    for (IInvokedMethod im : m_invokedMethods) {
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

  public List<ITestNGMethod> getInvokedMethods() {
    List<ITestNGMethod> result= Lists.newArrayList();
    for (IInvokedMethod im : m_invokedMethods) {
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
    @Override
    public void onConfigurationFailure(ITestResult itr) {
      m_failedConfigurations.addResult(itr, itr.getMethod());
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
      m_skippedConfigurations.addResult(itr, itr.getMethod());
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
      m_passedConfigurations.addResult(itr, itr.getMethod());
    }
  }

  public void setMethodInterceptor(IMethodInterceptor methodInterceptor) {
    m_methodInterceptor = methodInterceptor;
  }

  @Override
  public XmlTest getCurrentXmlTest() {
    return m_xmlTest;
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

} // TestRunner
