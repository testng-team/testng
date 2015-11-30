package org.testng;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Guice;
import org.testng.annotations.IListenersAnnotation;
import org.testng.collections.ListMultiMap;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.Attributes;
import org.testng.internal.ClassHelper;
import org.testng.internal.ClassImpl;
import org.testng.internal.ClassInfoMap;
import org.testng.internal.ConfigurationGroupMethods;
import org.testng.internal.Constants;
import org.testng.internal.DynamicGraph;
import org.testng.internal.DynamicGraph.Status;
import org.testng.internal.IConfiguration;
import org.testng.internal.IInvoker;
import org.testng.internal.ITestResultNotifier;
import org.testng.internal.InvokedMethod;
import org.testng.internal.Invoker;
import org.testng.internal.MethodGroupsHelper;
import org.testng.internal.MethodHelper;
import org.testng.internal.MethodInstance;
import org.testng.internal.ResultMap;
import org.testng.internal.RunInfo;
import org.testng.internal.TestMethodWorker;
import org.testng.internal.TestNGClassFinder;
import org.testng.internal.TestNGMethodFinder;
import org.testng.internal.Utils;
import org.testng.internal.XmlMethodSelector;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IListeners;
import org.testng.internal.thread.graph.GraphThreadPoolExecutor;
import org.testng.internal.thread.graph.IThreadWorkerFactory;
import org.testng.internal.thread.graph.IWorker;
import org.testng.junit.IJUnitTestRunner;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * This class takes care of running one Test.
 *
 * @author Cedric Beust, Apr 26, 2004
 */
public class TestRunner
    implements ITestContext, ITestResultNotifier, IThreadWorkerFactory<ITestNGMethod>
{
  /* generated */
  private static final long serialVersionUID = 4247820024988306670L;
  private ISuite m_suite;
  private XmlTest m_xmlTest;
  private String m_testName;

  transient private List<XmlClass> m_testClassesFromXml= null;
  transient private List<XmlPackage> m_packageNamesFromXml= null;

  transient private IInvoker m_invoker= null;
  transient private IAnnotationFinder m_annotationFinder= null;

  /** ITestListeners support. */
  transient private List<ITestListener> m_testListeners = Lists.newArrayList();
  transient private Set<IConfigurationListener> m_configurationListeners = Sets.newHashSet();

  transient private IConfigurationListener m_confListener= new ConfigurationListener();
  transient private boolean m_skipFailedInvocationCounts;

  transient private Collection<IInvokedMethodListener> m_invokedMethodListeners = Lists.newArrayList();
  transient private List<IClassListener> m_classListeners = Lists.newArrayList();

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
  transient private Map<Class<?>, ITestClass> m_classMap = Maps.newLinkedHashMap();

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
  transient private List<IMethodInterceptor> m_methodInterceptors;

  private transient ClassMethodMap m_classMethodMap;
  private transient TestNGClassFinder m_testClassFinder;
  private transient IConfiguration m_configuration;
  private IMethodInterceptor builtinInterceptor;

  protected TestRunner(IConfiguration configuration,
                    ISuite suite,
                    XmlTest test,
                    String outputDirectory,
                    IAnnotationFinder finder,
                    boolean skipFailedInvocationCounts,
                    Collection<IInvokedMethodListener> invokedMethodListeners,
                    List<IClassListener> classListeners)
  {
    init(configuration, suite, test, outputDirectory, finder, skipFailedInvocationCounts,
        invokedMethodListeners, classListeners);
  }

  public TestRunner(IConfiguration configuration, ISuite suite, XmlTest test,
      boolean skipFailedInvocationCounts,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      List<IClassListener> classListeners) {
    init(configuration, suite, test, suite.getOutputDirectory(),
        suite.getAnnotationFinder(),
        skipFailedInvocationCounts, invokedMethodListeners, classListeners);
  }

  private void init(IConfiguration configuration,
                    ISuite suite,
                    XmlTest test,
                    String outputDirectory,
                    IAnnotationFinder annotationFinder,
                    boolean skipFailedInvocationCounts,
                    Collection<IInvokedMethodListener> invokedMethodListeners,
                    List<IClassListener> classListeners)
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
    m_methodInterceptors = new ArrayList<IMethodInterceptor>();
    builtinInterceptor = preserveOrder ? new PreserveOrderMethodInterceptor() : new InstanceOrderingMethodInterceptor();

    m_packageNamesFromXml= test.getXmlPackages();
    if(null != m_packageNamesFromXml) {
      for(XmlPackage xp: m_packageNamesFromXml) {
        m_testClassesFromXml.addAll(xp.getXmlClasses());
      }
    }

    m_annotationFinder= annotationFinder;
    m_invokedMethodListeners = invokedMethodListeners;
    m_classListeners = classListeners;
    m_invoker = new Invoker(m_configuration, this, this, m_suite.getSuiteState(),
        m_skipFailedInvocationCounts, invokedMethodListeners, classListeners);

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

  private static class ListenerHolder {
    private List<Class<? extends ITestNGListener>> listenerClasses;
    private Class<? extends ITestNGListenerFactory> listenerFactoryClass;
  }

  /**
   * @return all the @Listeners annotations found in the current class and its
   * superclasses.
   */
  private ListenerHolder findAllListeners(Class<?> cls) {
    ListenerHolder result = new ListenerHolder();
    result.listenerClasses = Lists.newArrayList();

    do {
      IListenersAnnotation l = m_annotationFinder.findAnnotation(cls, IListenersAnnotation.class);
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
    Set<Class<? extends ITestNGListener>> listenerClasses = Sets.newHashSet();
    Class<? extends ITestNGListenerFactory> listenerFactoryClass = null;

    for (IClass cls : getTestClasses()) {
      Class<? extends ITestNGListenerFactory> realClass = cls.getRealClass();
      ListenerHolder listenerHolder = findAllListeners(realClass);
      if (listenerFactoryClass == null) {
        listenerFactoryClass = listenerHolder.listenerFactoryClass;
      }
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
      if (listener == null) {
        listener = ClassHelper.newInstance(c);
      }

      if (listener instanceof IMethodInterceptor) {
        m_methodInterceptors.add((IMethodInterceptor) listener);
      }
      if (listener instanceof ISuiteListener) {
        m_suite.addListener((ISuiteListener) listener);
      }
      if (listener instanceof IInvokedMethodListener) {
        m_suite.addListener((ITestNGListener) listener);
      }
      if (listener instanceof ITestListener) {
        // At this point, the field m_testListeners has already been used in the creation
        addTestListener((ITestListener) listener);
      }
      if (listener instanceof IClassListener) {
        m_classListeners.add((IClassListener) listener);
      }
      if (listener instanceof IConfigurationListener) {
        addConfigurationListener((IConfigurationListener) listener);
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
      if (listener instanceof IExecutionListener) {
        IExecutionListener iel = (IExecutionListener) listener;
        iel.onExecutionStart();
        m_configuration.addExecutionListener(iel);
      }
    }
  }

  /**
   * Initialize meta groups
   */
  private void initMetaGroups(XmlTest xmlTest) {
    Map<String, List<String>> metaGroups = xmlTest.getMetaGroups();

    for (Map.Entry<String, List<String>> entry : metaGroups.entrySet()) {
      addMetaGroup(entry.getKey(), entry.getValue());
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
                                             m_configuration,
                                             this);
    ITestMethodFinder testMethodFinder
      = new TestNGMethodFinder(m_runInfo, m_annotationFinder);

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
    Map<String, List<ITestNGMethod>> beforeGroupMethods =
        MethodGroupsHelper.findGroupsMethods(m_classMap.values(), true);
    Map<String, List<ITestNGMethod>> afterGroupMethods =
        MethodGroupsHelper.findGroupsMethods(m_classMap.values(), false);

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
    m_beforeSuiteMethods = MethodHelper.collectAndOrderMethods(beforeSuiteMethods,
                                                              false /* forTests */,
                                                              m_runInfo,
                                                              m_annotationFinder,
                                                              true /* unique */,
                                                              m_excludedMethods);

    m_beforeXmlTestMethods = MethodHelper.collectAndOrderMethods(beforeXmlTestMethods,
                                                              false /* forTests */,
                                                              m_runInfo,
                                                              m_annotationFinder,
                                                              true /* unique (CQ added by me)*/,
                                                              m_excludedMethods);

    m_allTestMethods = MethodHelper.collectAndOrderMethods(testMethods,
                                                                true /* forTest? */,
                                                                m_runInfo,
                                                                m_annotationFinder,
                                                                false /* unique */,
                                                                m_excludedMethods);
    m_classMethodMap = new ClassMethodMap(testMethods, m_xmlMethodSelector);

    m_afterXmlTestMethods = MethodHelper.collectAndOrderMethods(afterXmlTestMethods,
                                                              false /* forTests */,
                                                              m_runInfo,
                                                              m_annotationFinder,
                                                              true /* unique (CQ added by me)*/,
                                                              m_excludedMethods);

    m_afterSuiteMethods = MethodHelper.collectAndOrderMethods(afterSuiteMethods,
                                                              false /* forTests */,
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
                                     m_xmlTest.getAllParameters(),
                                     null, /* no parameter values */
                                     null /* instance */);
    }
  }

  private void privateRunJUnit(XmlTest xmlTest) {
    final ClassInfoMap cim = new ClassInfoMap(m_testClassesFromXml, false);
    final Set<Class<?>> classes = cim.getClasses();
    final List<ITestNGMethod> runMethods = Lists.newArrayList();
    List<IWorker<ITestNGMethod>> workers = Lists.newArrayList();
    // FIXME: directly referencing JUnitTestRunner which uses JUnit classes
    // may result in an class resolution exception under different JVMs
    // The resolution process is not specified in the JVM spec with a specific implementation,
    // so it can be eager => failure
    workers.add(new IWorker<ITestNGMethod>() {
      /**
       * @see TestMethodWorker#getTimeOut()
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
          List<XmlInclude> includedMethods = cim.getXmlClass(tc).getIncludedMethods();
          List<String> methods = Lists.newArrayList();
          for (XmlInclude inc: includedMethods) {
              methods.add(inc.getName());
          }
          IJUnitTestRunner tr= ClassHelper.createTestRunner(TestRunner.this);
          tr.setInvokedMethodListeners(m_invokedMethodListeners);
          try {
            tr.run(tc, methods.toArray(new String[methods.size()]));
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
        if (m_allTestMethods.length == 1) {
          return m_allTestMethods[0].getPriority();
        } else {
          return 0;
        }
      }

      @Override
      public int compareTo(IWorker<ITestNGMethod> other) {
        return getPriority() - other.getPriority();
      }
    });

    runJUnitWorkers(workers);
    m_allTestMethods= runMethods.toArray(new ITestNGMethod[runMethods.size()]);
  }

  private static final EnumSet<XmlSuite.ParallelMode> PRIVATE_RUN_PARALLEL_MODES
      = EnumSet.of(XmlSuite.ParallelMode.METHODS, XmlSuite.ParallelMode.TRUE,
                   XmlSuite.ParallelMode.CLASSES, XmlSuite.ParallelMode.INSTANCES);
  /**
   * Main method that create a graph of methods and then pass it to the
   * graph executor to run them.
   */
  private void privateRun(XmlTest xmlTest) {
    XmlSuite.ParallelMode parallelMode = xmlTest.getParallel();
    boolean parallel = PRIVATE_RUN_PARALLEL_MODES.contains(parallelMode);

    {
      // parallel
      int threadCount = parallel ? xmlTest.getThreadCount() : 1;
      // Make sure we create a graph based on the intercepted methods, otherwise an interceptor
      // removing methods would cause the graph never to terminate (because it would expect
      // termination from methods that never get invoked).
      DynamicGraph<ITestNGMethod> graph = createDynamicGraph(intercept(m_allTestMethods));
      if (parallel) {
        if (graph.getNodeCount() > 0) {
          GraphThreadPoolExecutor<ITestNGMethod> executor =
                  new GraphThreadPoolExecutor<>(graph, this,
                          threadCount, threadCount, 0, TimeUnit.MILLISECONDS,
                          new LinkedBlockingQueue<Runnable>());
          executor.run();
          try {
            long timeOut = m_xmlTest.getTimeOut(XmlTest.DEFAULT_TIMEOUT_MS);
            Utils.log("TestRunner", 2, "Starting executor for test " + m_xmlTest.getName()
                + " with time out:" + timeOut + " milliseconds.");
            executor.awaitTermination(timeOut, TimeUnit.MILLISECONDS);
            executor.shutdownNow();
          } catch (InterruptedException handled) {
            handled.printStackTrace();
            Thread.currentThread().interrupt();
          }
        }
      } else {
        boolean debug = false;
        List<ITestNGMethod> freeNodes = graph.getFreeNodes();
        if (debug) {
          System.out.println("Free nodes:" + freeNodes);
        }

        if (graph.getNodeCount() > 0 && freeNodes.isEmpty()) {
          throw new TestNGException("No free nodes found in:" + graph);
        }

        while (! freeNodes.isEmpty()) {
          List<IWorker<ITestNGMethod>> runnables = createWorkers(freeNodes);
          for (IWorker<ITestNGMethod> r : runnables) {
            r.run();
          }
          graph.setStatus(freeNodes, Status.FINISHED);
          freeNodes = graph.getFreeNodes();
          if (debug) {
            System.out.println("Free nodes:" + freeNodes);
          }
        }
      }
    }
  }

  /**
   * Apply the method interceptor (if applicable) to the list of methods.
   */
  private ITestNGMethod[] intercept(ITestNGMethod[] methods) {
    List<IMethodInstance> methodInstances = methodsToMethodInstances(Arrays.asList(methods));

    // add built-in interceptor (PreserveOrderMethodInterceptor or InstanceOrderingMethodInterceptor at the end of the list
    m_methodInterceptors.add(builtinInterceptor);
    for (IMethodInterceptor m_methodInterceptor : m_methodInterceptors) {
      methodInstances = m_methodInterceptor.intercept(methodInstances, this);
    }

    List<ITestNGMethod> result = Lists.newArrayList();
    for (IMethodInstance imi : methodInstances) {
      result.add(imi.getMethod());
    }
    
    //Since an interceptor is involved, we would need to ensure that the ClassMethodMap object is in sync with the 
    //output of the interceptor, else @AfterClass doesn't get executed at all when interceptors are involved.
    //so let's update the current classMethodMap object with the list of methods obtained from the interceptor.
    this.m_classMethodMap = new ClassMethodMap(result, null);
    
    return result.toArray(new ITestNGMethod[result.size()]);
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
  public List<IWorker<ITestNGMethod>> createWorkers(List<ITestNGMethod> methods) {
    List<IWorker<ITestNGMethod>> result;
    if (XmlSuite.ParallelMode.INSTANCES.equals(m_xmlTest.getParallel())) {
      result = createInstanceBasedParallelWorkers(methods);
    } else {
      result = createClassBasedParallelWorkers(methods);
    }
    return result;
  }

  /**
   * Create workers for parallel="classes" and similar cases.
   */
  private List<IWorker<ITestNGMethod>> createClassBasedParallelWorkers(List<ITestNGMethod> methods) {
    List<IWorker<ITestNGMethod>> result = Lists.newArrayList();
    // Methods that belong to classes with a sequential=true or parallel=classes
    // attribute must all be run in the same worker
    Set<Class> sequentialClasses = Sets.newHashSet();
    for (ITestNGMethod m : methods) {
      Class<? extends ITestClass> cls = m.getRealClass();
      org.testng.annotations.ITestAnnotation test =
          m_annotationFinder.findAnnotation(cls, org.testng.annotations.ITestAnnotation.class);

      // If either sequential=true or parallel=classes, mark this class sequential
      if (test != null && (test.getSequential() || test.getSingleThreaded()) ||
          XmlSuite.ParallelMode.CLASSES.equals(m_xmlTest.getParallel())) {
        sequentialClasses.add(cls);
      }
    }

    List<IMethodInstance> methodInstances = Lists.newArrayList();
    for (ITestNGMethod tm : methods) {
      methodInstances.addAll(methodsToMultipleMethodInstances(tm));
    }


    Map<String, String> params = m_xmlTest.getAllParameters();

    Set<Class<?>> processedClasses = Sets.newHashSet();
    for (IMethodInstance im : methodInstances) {
      Class<?> c = im.getMethod().getTestClass().getRealClass();
      if (sequentialClasses.contains(c)) {
        if (!processedClasses.contains(c)) {
          processedClasses.add(c);
          if (System.getProperty("experimental") != null) {
            List<List<IMethodInstance>> instances = createInstances(methodInstances);
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


  /**
   * Create workers for parallel="instances".
   */
  private List<IWorker<ITestNGMethod>>
      createInstanceBasedParallelWorkers(List<ITestNGMethod> methods) {
    List<IWorker<ITestNGMethod>> result = Lists.newArrayList();
    ListMultiMap<Object, ITestNGMethod> lmm = Maps.newListMultiMap();
    for (ITestNGMethod m : methods) {
      lmm.put(m.getInstance(), m);
    }
    for (Map.Entry<Object, List<ITestNGMethod>> es : lmm.entrySet()) {
      List<IMethodInstance> methodInstances = Lists.newArrayList();
      for (ITestNGMethod m : es.getValue()) {
        methodInstances.add(new MethodInstance(m));
      }
      TestMethodWorker tmw = new TestMethodWorker(m_invoker,
          methodInstances.toArray(new IMethodInstance[methodInstances.size()]),
          m_xmlTest.getSuite(),
          m_xmlTest.getAllParameters(),
          m_groupMethods,
          m_classMethodMap,
          this,
          m_classListeners);
      result.add(tmw);
    }

    return result;
  }

  private List<List<IMethodInstance>> createInstances(List<IMethodInstance> methodInstances) {
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
//    System.out.println(map);
    return new ArrayList<>(map.values());
  }

  private TestMethodWorker createTestMethodWorker(
      List<IMethodInstance> methodInstances, Map<String, String> params,
      Class<?> c) {
    return new TestMethodWorker(m_invoker,
        findClasses(methodInstances, c),
        m_xmlTest.getSuite(),
        params,
        m_groupMethods,
        m_classMethodMap,
        this,
        m_classListeners);
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

  /**
   * @@@ remove this
   */
  private List<MethodInstance> methodsToMultipleMethodInstances(ITestNGMethod... sl) {
    List<MethodInstance> vResult = Lists.newArrayList();
    for (ITestNGMethod m : sl) {
      vResult.add(new MethodInstance(m));
    }

    return vResult;
  }

  private List<IMethodInstance> methodsToMethodInstances(List<ITestNGMethod> sl) {
    List<IMethodInstance> result = new ArrayList<>();
      for (ITestNGMethod iTestNGMethod : sl) {
        result.add(new MethodInstance(iTestNGMethod));
      }
    return result;
  }

  //
  // Invoke the workers
  //
  private static final EnumSet<XmlSuite.ParallelMode> WORKERS_PARALLEL_MODES
      = EnumSet.of(XmlSuite.ParallelMode.METHODS, XmlSuite.ParallelMode.TRUE,
                   XmlSuite.ParallelMode.CLASSES);
  private void runJUnitWorkers(List<? extends IWorker<ITestNGMethod>> workers) {
      //
      // Sequential run
      //
      for (IWorker<ITestNGMethod> tmw : workers) {
        tmw.run();
      }
  }

  private void afterRun() {
    // invoke @AfterTest
    ITestNGMethod[] testConfigurationMethods= getAfterTestConfigurationMethods();
    if(null != testConfigurationMethods && testConfigurationMethods.length > 0) {
      m_invoker.invokeConfigurations(null,
                                     testConfigurationMethods,
                                     m_xmlTest.getSuite(),
                                     m_xmlTest.getAllParameters(),
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

  private DynamicGraph<ITestNGMethod> createDynamicGraph(ITestNGMethod[] methods) {
    DynamicGraph<ITestNGMethod> result = new DynamicGraph<>();
    result.setComparator(new Comparator<ITestNGMethod>() {
      @Override
      public int compare(ITestNGMethod o1, ITestNGMethod o2) {
        return o1.getPriority() - o2.getPriority();
      }
    });

    DependencyMap dependencyMap = new DependencyMap(methods);

    // Keep track of whether we have group dependencies. If we do, preserve-order needs
    // to be ignored since group dependencies create inter-class dependencies which can
    // end up creating cycles when combined with preserve-order.
    boolean hasDependencies = false;
    for (ITestNGMethod m : methods) {
      result.addNode(m);

      // Dependent methods
      {
        String[] dependentMethods = m.getMethodsDependedUpon();
        if (dependentMethods != null) {
          for (String d : dependentMethods) {
            ITestNGMethod dm = dependencyMap.getMethodDependingOn(d, m);
            if (m != dm){
            	result.addEdge(m, dm);
            }
          }
        }
      }

      // Dependent groups
      {
        String[] dependentGroups = m.getGroupsDependedUpon();
        for (String d : dependentGroups) {
          hasDependencies = true;
          List<ITestNGMethod> dg = dependencyMap.getMethodsThatBelongTo(d, m);
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

    // Preserve order
    // Don't preserve the ordering if we're running in parallel, otherwise the suite will
    // create multiple threads but these threads will be created one after the other,
    // giving the impression of parallelism (multiple thread id's) while still running
    // sequentially.
    if (! hasDependencies
        && getCurrentXmlTest().getParallel() == XmlSuite.ParallelMode.FALSE
        && "true".equalsIgnoreCase(getCurrentXmlTest().getPreserveOrder())) {
      // If preserve-order was specified and the class order is A, B
      // create a new set of dependencies where each method of B depends
      // on all the methods of A
      ListMultiMap<ITestNGMethod, ITestNGMethod> classDependencies
          = createClassDependencies(methods, getCurrentXmlTest());

      for (Map.Entry<ITestNGMethod, List<ITestNGMethod>> es : classDependencies.entrySet()) {
        for (ITestNGMethod dm : es.getValue()) {
          result.addEdge(dm, es.getKey());
        }
      }
    }

    // Group by instances
    if (getCurrentXmlTest().getGroupByInstances()) {
      ListMultiMap<ITestNGMethod, ITestNGMethod> instanceDependencies
          = createInstanceDependencies(methods, getCurrentXmlTest());

      for (Map.Entry<ITestNGMethod, List<ITestNGMethod>> es : instanceDependencies.entrySet()) {
        for (ITestNGMethod dm : es.getValue()) {
          result.addEdge(dm, es.getKey());
        }
      }

    }

    return result;
  }

  private ListMultiMap<ITestNGMethod, ITestNGMethod> createInstanceDependencies(
      ITestNGMethod[] methods, XmlTest currentXmlTest)
  {
    ListMultiMap<Object, ITestNGMethod> instanceMap = Maps.newListMultiMap();
    for (ITestNGMethod m : methods) {
      instanceMap.put(m.getInstance(), m);
    }

    ListMultiMap<ITestNGMethod, ITestNGMethod> result = Maps.newListMultiMap();
    Object previousInstance = null;
    for (Map.Entry<Object, List<ITestNGMethod>> es : instanceMap.entrySet()) {
      if (previousInstance == null) {
        previousInstance = es.getKey();
      } else {
        List<ITestNGMethod> previousMethods = instanceMap.get(previousInstance);
        Object currentInstance = es.getKey();
        List<ITestNGMethod> currentMethods = instanceMap.get(currentInstance);
        // Make all the methods from the current instance depend on the methods of
        // the previous instance
        for (ITestNGMethod cm : currentMethods) {
          for (ITestNGMethod pm : previousMethods) {
            result.put(cm, pm);
          }
        }
        previousInstance = currentInstance;
      }
    }

    return result;
  }

  private ListMultiMap<ITestNGMethod, ITestNGMethod> createClassDependencies(
      ITestNGMethod[] methods, XmlTest test)
  {
    Map<String, List<ITestNGMethod>> classes = Maps.newHashMap();
    // Note: use a List here to preserve the ordering but make sure
    // we don't add the same class twice
    List<XmlClass> sortedClasses = Lists.newArrayList();

    for (XmlClass c : test.getXmlClasses()) {
      classes.put(c.getName(), new ArrayList<ITestNGMethod>());
      if (! sortedClasses.contains(c)) sortedClasses.add(c);
    }

    // Sort the classes based on their order of appearance in the XML
    Collections.sort(sortedClasses, new Comparator<XmlClass>() {
      @Override
      public int compare(XmlClass arg0, XmlClass arg1) {
        return arg0.getIndex() - arg1.getIndex();
      }
    });

    Map<String, Integer> indexedClasses1 = Maps.newHashMap();
    Map<Integer, String> indexedClasses2 = Maps.newHashMap();
    int i = 0;
    for (XmlClass c : sortedClasses) {
      indexedClasses1.put(c.getName(), i);
      indexedClasses2.put(i, c.getName());
      i++;
    }

    ListMultiMap<String, ITestNGMethod> methodsFromClass = Maps.newListMultiMap();
    for (ITestNGMethod m : methods) {
      methodsFromClass.put(m.getTestClass().getName(), m);
    }

    ListMultiMap<ITestNGMethod, ITestNGMethod> result = Maps.newListMultiMap();
    for (ITestNGMethod m : methods) {
      String name = m.getTestClass().getName();
      Integer index = indexedClasses1.get(name);
      // The index could be null if the classes listed in the XML are different
      // from the methods being run (e.g. the .xml only contains a factory that
      // instantiates methods from a different class). In this case, we cannot
      // perform any ordering.
      if (index != null && index > 0) {
        // Make this method depend on all the methods of the class in the previous
        // index
        String classDependedUpon = indexedClasses2.get(index - 1);
        List<ITestNGMethod> methodsDependedUpon = methodsFromClass.get(classDependedUpon);
        if (methodsDependedUpon != null) {
          for (ITestNGMethod mdu : methodsDependedUpon) {
            result.put(mdu, m);
          }
        }
      }
    }

    return result;
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
  public Set<ITestResult> getSkippedTests(ITestNGMethod tm) {
    return m_skippedTests.getResults(tm);
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
    return Lists.<IConfigurationListener>newArrayList(m_configurationListeners);
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
    if(listener instanceof IClassListener) {
      addClassListener((IClassListener) listener);
    }
  }

  public void addTestListener(ITestListener il) {
    m_testListeners.add(il);
  }

  public void addClassListener(IClassListener cl) {
    m_classListeners.add(cl);
  }

  void addConfigurationListener(IConfigurationListener icl) {
    m_configurationListeners.add(icl);
  }
  //
  // Listeners
  /////

  private final List<InvokedMethod> m_invokedMethods = Lists.newArrayList();

  private void dumpInvokedMethods() {
    System.out.println("===== Invoked methods");
    for (IInvokedMethod im : m_invokedMethods) {
      if (im.isTestMethod()) {
        System.out.print("    ");
      }
      else if (im.isConfigurationMethod()) {
        System.out.print("  ");
      }
      else {
        continue;
      }
      System.out.println("" + im);
    }
    System.out.println("=====");
  }

  public List<ITestNGMethod> getInvokedMethods() {
    List<ITestNGMethod> result= Lists.newArrayList();
    synchronized(m_invokedMethods) {
      for (IInvokedMethod im : m_invokedMethods) {
        ITestNGMethod tm= im.getTestMethod();
        tm.setDate(im.getDate());
        result.add(tm);
      }
    }

    return result;
  }

  private IResultMap m_passedConfigurations= new ResultMap();
  private IResultMap m_skippedConfigurations= new ResultMap();
  private IResultMap m_failedConfigurations= new ResultMap();

  private class ConfigurationListener implements IConfigurationListener2 {
    @Override
    public void beforeConfiguration(ITestResult tr) {
    }

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

  @Deprecated
  public void setMethodInterceptor(IMethodInterceptor methodInterceptor){
    m_methodInterceptors.add(methodInterceptor);
  }

  public void addMethodInterceptor(IMethodInterceptor methodInterceptor){
    m_methodInterceptors.add(methodInterceptor);
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

  private ListMultiMap<Class<? extends Module>, Module> m_guiceModules = Maps.newListMultiMap();

  @Override
  public List<Module> getGuiceModules(Class<? extends Module> cls) {
    List<Module> result = m_guiceModules.get(cls);
    return result;
  }

  private void addGuiceModule(Class<? extends Module> cls, Module module) {
    m_guiceModules.put(cls, module);
  }

  private Map<List<Module>, Injector> m_injectors = Maps.newHashMap();

  @Override
  public Injector getInjector(List<Module> moduleInstances) {
    return m_injectors .get(moduleInstances);
  }

  @Override
  public Injector getInjector(IClass iClass) {
    Annotation annotation = AnnotationHelper.findAnnotationSuperClasses(Guice.class, iClass.getRealClass());
    if (annotation == null) return null;
    if (iClass instanceof TestClass) {
      iClass = ((TestClass)iClass).getIClass();
    }
    if (!(iClass instanceof ClassImpl)) return null;
    Injector parentInjector = ((ClassImpl)iClass).getParentInjector();

    Guice guice = (Guice) annotation;
    List<Module> moduleInstances = Lists.newArrayList(getModules(guice, parentInjector, iClass.getRealClass()));

    // Reuse the previous injector, if any
    Injector injector = getInjector(moduleInstances);
    if (injector == null) {
      injector = parentInjector.createChildInjector(moduleInstances);
      addInjector(moduleInstances, injector);
    }
    return injector;
  }

  private Module[] getModules(Guice guice, Injector parentInjector, Class<?> testClass) {
    List<Module> result = Lists.newArrayList();
    for (Class<? extends Module> moduleClass : guice.modules()) {
      List<Module> modules = getGuiceModules(moduleClass);
      if (modules != null && modules.size() > 0) {
        result.addAll(modules);
      } else {
        Module instance = parentInjector.getInstance(moduleClass);
        result.add(instance);
        addGuiceModule(moduleClass, instance);
      }
    }
    Class<? extends IModuleFactory> factory = guice.moduleFactory();
    if (factory != IModuleFactory.class) {
      IModuleFactory factoryInstance = parentInjector.getInstance(factory);
      Module moduleClass = factoryInstance.createModule(this, testClass);
      if (moduleClass != null) {
        result.add(moduleClass);
      }
    }

    return result.toArray(new Module[result.size()]);
  }

  @Override
  public void addInjector(List<Module> moduleInstances, Injector injector) {
    m_injectors.put(moduleInstances, injector);
  }

} // TestRunner
