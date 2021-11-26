package org.testng;

import static org.testng.internal.MethodHelper.fixMethodsWithClass;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.Attributes;
import org.testng.internal.ClassInfoMap;
import org.testng.internal.ConfigurationGroupMethods;
import org.testng.internal.DefaultListenerFactory;
import org.testng.internal.DynamicGraphHelper;
import org.testng.internal.GroupsHelper;
import org.testng.internal.IConfigEavesdropper;
import org.testng.internal.IConfiguration;
import org.testng.internal.IContainer;
import org.testng.internal.ITestClassConfigInfo;
import org.testng.internal.ITestResultNotifier;
import org.testng.internal.MethodGroupsHelper;
import org.testng.internal.MethodHelper;
import org.testng.internal.ResultMap;
import org.testng.internal.RunInfo;
import org.testng.internal.RuntimeBehavior;
import org.testng.internal.Systematiser;
import org.testng.internal.TestListenerHelper;
import org.testng.internal.TestMethodComparator;
import org.testng.internal.TestMethodContainer;
import org.testng.internal.TestNGClassFinder;
import org.testng.internal.TestNGMethodFinder;
import org.testng.internal.Utils;
import org.testng.internal.XmlMethodSelector;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.invokers.AbstractParallelWorker;
import org.testng.internal.invokers.ConfigMethodArguments;
import org.testng.internal.invokers.ConfigMethodArguments.Builder;
import org.testng.internal.invokers.IInvoker;
import org.testng.internal.invokers.Invoker;
import org.testng.internal.invokers.TestMethodWorker;
import org.testng.internal.objects.IObjectDispenser;
import org.testng.junit.IJUnitTestRunner;
import org.testng.log4testng.Logger;
import org.testng.thread.ITestNGThreadPoolExecutor;
import org.testng.thread.IThreadWorkerFactory;
import org.testng.thread.IWorker;
import org.testng.util.Strings;
import org.testng.util.TimeUtils;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlTest;

/** This class takes care of running one Test. */
public class TestRunner
    implements ITestContext,
        ITestResultNotifier,
        IThreadWorkerFactory<ITestNGMethod>,
        IConfigEavesdropper {

  private static final String DEFAULT_PROP_OUTPUT_DIR = "test-output";
  private static final Logger LOGGER = Logger.getLogger(TestRunner.class);

  private final Comparator<ITestNGMethod> comparator;
  private ISuite m_suite;
  private XmlTest m_xmlTest;
  private String m_testName;
  private IInjectorFactory m_injectorFactory;
  private ITestObjectFactory m_objectFactory;

  private List<XmlClass> m_testClassesFromXml = null;

  private IInvoker m_invoker = null;
  private IAnnotationFinder m_annotationFinder = null;

  /** ITestListeners support. */
  private final List<ITestListener> m_testListeners = Lists.newArrayList();

  private final Set<IConfigurationListener> m_configurationListeners = Sets.newLinkedHashSet();
  private final Set<IExecutionVisualiser> visualisers = Sets.newHashSet();

  private final IConfigurationListener m_confListener = new ConfigurationListener();

  private Collection<IInvokedMethodListener> m_invokedMethodListeners = Lists.newArrayList();
  private final Map<Class<? extends IClassListener>, IClassListener> m_classListeners =
      Maps.newLinkedHashMap();
  private final DataProviderHolder holder = new DataProviderHolder();

  private Date m_startDate = new Date();
  private Date m_endDate = null;
  private final IContainer<ITestNGMethod> testMethodsContainer =
      new TestMethodContainer(this::computeAndGetAllTestMethods);

  /** A map to keep track of Class <-> IClass. */
  private final Map<Class<?>, ITestClass> m_classMap = Maps.newLinkedHashMap();

  /** Where the reports will be created. */
  private String m_outputDirectory = DEFAULT_PROP_OUTPUT_DIR;

  // The XML method selector (groups/methods included/excluded in XML)
  private final XmlMethodSelector m_xmlMethodSelector = new XmlMethodSelector();

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
  private final List<ITestNGMethod> m_excludedMethods = Lists.newArrayList();
  private ConfigurationGroupMethods m_groupMethods = null;

  // Meta groups
  private final Map<String, List<String>> m_metaGroups = Maps.newHashMap();

  // All the tests that were run along with their result
  private final IResultMap m_passedTests = new ResultMap();
  private final IResultMap m_failedTests = new ResultMap();
  private final IResultMap m_failedButWithinSuccessPercentageTests = new ResultMap();
  private final IResultMap m_skippedTests = new ResultMap();

  private final RunInfo m_runInfo = new RunInfo(this::getCurrentXmlTest);

  // The host where this test was run, or null if run locally
  private String m_host;

  // Defined dynamically depending on <test preserve-order="true/false">
  private List<IMethodInterceptor> m_methodInterceptors;

  private ClassMethodMap m_classMethodMap;
  private TestNGClassFinder m_testClassFinder;
  private IConfiguration m_configuration;

  public enum PriorityWeight {
    groupByInstance,
    preserveOrder,
    priority,
    dependsOnGroups,
    dependsOnMethods
  }

  protected TestRunner(
      IConfiguration configuration,
      ISuite suite,
      XmlTest test,
      String outputDirectory,
      IAnnotationFinder finder,
      boolean skipFailedInvocationCounts,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      List<IClassListener> classListeners,
      Comparator<ITestNGMethod> comparator,
      DataProviderHolder otherHolder) {
    this.comparator = comparator;
    this.holder.merge(otherHolder);
    init(
        configuration,
        suite,
        test,
        outputDirectory,
        finder,
        skipFailedInvocationCounts,
        invokedMethodListeners,
        classListeners);
  }

  public TestRunner(
      IConfiguration configuration,
      ISuite suite,
      XmlTest test,
      boolean skipFailedInvocationCounts,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      List<IClassListener> classListeners,
      Comparator<ITestNGMethod> comparator) {
    this.comparator = comparator;
    init(
        configuration,
        suite,
        test,
        suite.getOutputDirectory(),
        suite.getAnnotationFinder(),
        skipFailedInvocationCounts,
        invokedMethodListeners,
        classListeners);
  }

  /* /!\ This constructor is used by testng-remote, any changes related to it please contact with testng-team. */
  public TestRunner(
      IConfiguration configuration,
      ISuite suite,
      XmlTest test,
      boolean skipFailedInvocationCounts,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      List<IClassListener> classListeners) {
    this.comparator = Systematiser.getComparator();
    init(
        configuration,
        suite,
        test,
        suite.getOutputDirectory(),
        suite.getAnnotationFinder(),
        skipFailedInvocationCounts,
        invokedMethodListeners,
        classListeners);
  }

  private void init(
      IConfiguration configuration,
      ISuite suite,
      XmlTest test,
      String outputDirectory,
      IAnnotationFinder annotationFinder,
      boolean skipFailedInvocationCounts,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      List<IClassListener> classListeners) {
    m_configuration = configuration;
    m_xmlTest = test;
    m_suite = suite;
    m_testName = test.getName();
    m_host = suite.getHost();
    m_testClassesFromXml = test.getXmlClasses();
    m_injectorFactory = m_configuration.getInjectorFactory();
    m_objectFactory = suite.getObjectFactory();
    setVerbose(test.getVerbose());

    boolean preserveOrder = test.getPreserveOrder();
    IMethodInterceptor builtinInterceptor =
        preserveOrder
            ? new PreserveOrderMethodInterceptor()
            : new InstanceOrderingMethodInterceptor();
    m_methodInterceptors = new ArrayList<>();
    // Add the built in interceptor as the first interceptor. That way we let our users determine
    // the final order
    // by plugging in their own custom interceptors as well.
    m_methodInterceptors.add(builtinInterceptor);

    List<XmlPackage> m_packageNamesFromXml = getAllPackages();
    for (XmlPackage xp : m_packageNamesFromXml) {
      m_testClassesFromXml.addAll(xp.getXmlClasses());
    }

    m_annotationFinder = annotationFinder;
    m_invokedMethodListeners = invokedMethodListeners;
    m_classListeners.clear();
    for (IClassListener classListener : classListeners) {
      m_classListeners.put(classListener.getClass(), classListener);
    }
    m_invoker =
        new Invoker(
            m_configuration,
            this,
            this,
            m_suite.getSuiteState(),
            skipFailedInvocationCounts,
            invokedMethodListeners,
            classListeners,
            holder);

    if (test.getParallel() != null) {
      log("Running the tests in '" + test.getName() + "' with parallel mode:" + test.getParallel());
    }

    setOutputDirectory(outputDirectory);

    // Finish our initialization
    init();
  }

  /**
   * Returns all packages to use for the current test. This includes the test from the test suite.
   * Never returns null.
   */
  private List<XmlPackage> getAllPackages() {
    final List<XmlPackage> allPackages = Lists.newArrayList();
    final List<XmlPackage> suitePackages = this.m_xmlTest.getSuite().getPackages();
    if (suitePackages != null) {
      allPackages.addAll(suitePackages);
    }
    final List<XmlPackage> testPackages = this.m_xmlTest.getPackages();
    if (testPackages != null) {
      allPackages.addAll(testPackages);
    }
    return allPackages;
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
    if (!m_xmlTest.isJUnit()) {
      initMethods();
    }

    initListeners();
    addConfigurationListener(m_confListener);
    for (IConfigurationListener cl : m_configuration.getConfigurationListeners()) {
      addConfigurationListener(cl);
    }
  }

  private void initListeners() {
    //
    // Find all the listener factories and collect all the listeners requested in a
    // @Listeners annotation.
    //
    Set<Class<? extends ITestNGListener>> listenerClasses = Sets.newHashSet();
    Class<? extends ITestNGListenerFactory> listenerFactoryClass = null;

    for (IClass cls : getTestClasses()) {
      Class<?> realClass = cls.getRealClass();
      TestListenerHelper.ListenerHolder listenerHolder =
          TestListenerHelper.findAllListeners(realClass, m_annotationFinder);
      if (listenerFactoryClass == null) {
        listenerFactoryClass = listenerHolder.getListenerFactoryClass();
      }
      listenerClasses.addAll(listenerHolder.getListenerClasses());
    }

    if (listenerFactoryClass == null) {
      listenerFactoryClass = DefaultListenerFactory.class;
    }

    //
    // Now we have all the listeners collected from @Listeners and at most one
    // listener factory collected from a class implementing ITestNGListenerFactory.
    // Instantiate all the requested listeners.
    //

    ITestNGListenerFactory factory =
        TestListenerHelper.createListenerFactory(
            m_objectFactory, m_testClassFinder, listenerFactoryClass, this);

    // Instantiate all the listeners
    for (Class<? extends ITestNGListener> c : listenerClasses) {
      if (IClassListener.class.isAssignableFrom(c) && m_classListeners.containsKey(c)) {
        continue;
      }
      ITestNGListener listener = factory.createListener(c);

      addListener(listener);
    }
  }

  /** Initialize meta groups */
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
    m_xmlMethodSelector.setScript(m_xmlTest.getScript());

    // Groups override
    m_xmlMethodSelector.setOverrideIncludedMethods(m_configuration.getOverrideIncludedMethods());

    // Methods
    m_xmlMethodSelector.setXmlClasses(m_xmlTest.getXmlClasses());

    m_runInfo.addMethodSelector(m_xmlMethodSelector, 10);

    // Add user-specified method selectors (only class selectors, we can ignore
    // script selectors here)
    if (null != xmlTest.getMethodSelectors()) {
      for (org.testng.xml.XmlMethodSelector selector : xmlTest.getMethodSelectors()) {
        if (selector.getClassName() != null) {
          IMethodSelector s;
          try {
            s = m_objectFactory.newInstance(selector.getClassName());
          } catch (Exception ex) {
            throw new TestNGException(
                "Couldn't find method selector : " + selector.getClassName(), ex);
          }

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
    m_testClassFinder =
        new TestNGClassFinder(classMap, Maps.newHashMap(), m_configuration, this, holder);
    ITestMethodFinder testMethodFinder =
        new TestNGMethodFinder(m_objectFactory, m_runInfo, m_annotationFinder, comparator);

    m_runInfo.setTestMethods(testMethods);

    //
    // Initialize TestClasses
    //
    IClass[] classes = m_testClassFinder.findTestClasses();

    for (IClass ic : classes) {

      // Create TestClass
      ITestClass tc =
          new TestClass(
              m_objectFactory,
              ic,
              testMethodFinder,
              m_annotationFinder,
              m_xmlTest,
              classMap.getXmlClass(ic.getRealClass()),
              m_testClassFinder.getFactoryCreationFailedMessage());
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
      fixMethodsWithClass(
          ((ITestClassConfigInfo) tc).getAllBeforeClassMethods().toArray(new ITestNGMethod[0]),
          tc,
          beforeClassMethods);
      fixMethodsWithClass(tc.getBeforeTestMethods(), tc, null);
      fixMethodsWithClass(tc.getAfterTestMethods(), tc, null);
      fixMethodsWithClass(tc.getAfterClassMethods(), tc, afterClassMethods);
      fixMethodsWithClass(tc.getBeforeSuiteMethods(), tc, beforeSuiteMethods);
      fixMethodsWithClass(tc.getAfterSuiteMethods(), tc, afterSuiteMethods);
      fixMethodsWithClass(tc.getBeforeTestConfigurationMethods(), tc, beforeXmlTestMethods);
      fixMethodsWithClass(tc.getAfterTestConfigurationMethods(), tc, afterXmlTestMethods);
      fixMethodsWithClass(
          tc.getBeforeGroupsMethods(),
          tc,
          MethodHelper.uniqueMethodList(beforeGroupMethods.values()));
      fixMethodsWithClass(
          tc.getAfterGroupsMethods(),
          tc,
          MethodHelper.uniqueMethodList(afterGroupMethods.values()));
    }

    //
    // Sort the methods
    //
    m_beforeSuiteMethods =
        MethodHelper.collectAndOrderMethods(
            beforeSuiteMethods,
            false /* forTests */,
            m_runInfo,
            m_annotationFinder,
            true /* unique */,
            m_excludedMethods,
            comparator);

    m_beforeXmlTestMethods =
        MethodHelper.collectAndOrderMethods(
            beforeXmlTestMethods,
            false /* forTests */,
            m_runInfo,
            m_annotationFinder,
            true /* unique (CQ added by me)*/,
            m_excludedMethods,
            comparator);

    m_classMethodMap =
        new ClassMethodMap(Arrays.asList(testMethodsContainer.getItems()), m_xmlMethodSelector);
    m_groupMethods =
        new ConfigurationGroupMethods(testMethodsContainer, beforeGroupMethods, afterGroupMethods);

    m_afterXmlTestMethods =
        MethodHelper.collectAndOrderMethods(
            afterXmlTestMethods,
            false /* forTests */,
            m_runInfo,
            m_annotationFinder,
            true /* unique (CQ added by me)*/,
            m_excludedMethods,
            comparator);

    m_afterSuiteMethods =
        MethodHelper.collectAndOrderMethods(
            afterSuiteMethods,
            false /* forTests */,
            m_runInfo,
            m_annotationFinder,
            true /* unique */,
            m_excludedMethods,
            comparator);
  }

  private ITestNGMethod[] computeAndGetAllTestMethods() {
    List<ITestNGMethod> testMethods = Lists.newArrayList();
    for (ITestClass tc : m_classMap.values()) {
      fixMethodsWithClass(tc.getTestMethods(), tc, testMethods);
    }

    return MethodHelper.collectAndOrderMethods(
        testMethods,
        true /* forTest? */,
        m_runInfo,
        m_annotationFinder,
        false /* unique */,
        m_excludedMethods,
        comparator);
  }

  public Collection<ITestClass> getTestClasses() {
    return m_classMap.values();
  }

  public void setTestName(String name) {
    m_testName = name;
  }

  public void setOutputDirectory(String od) {
    m_outputDirectory = od;
  }

  private void addMetaGroup(String name, List<String> groupNames) {
    m_metaGroups.put(name, groupNames);
  }

  private Map<String, String> createGroups(List<String> groups) {
    return GroupsHelper.createGroups(m_metaGroups, groups);
  }

  /**
   * The main entry method for TestRunner.
   *
   * <p>This is where all the hard work is done: - Invoke configuration methods - Invoke test
   * methods - Catch exceptions - Collect results - Invoke listeners - etc...
   */
  public void run() {
    beforeRun();

    try {
      XmlTest test = getTest();
      if (test.isJUnit()) {
        privateRunJUnit();
      } else {
        privateRun(test);
      }
    } finally {
      afterRun();
      forgetHeavyReferencesIfNeeded();
    }
  }

  private void forgetHeavyReferencesIfNeeded() {
    if (RuntimeBehavior.isMemoryFriendlyMode()) {
      testMethodsContainer.clearItems();
      m_groupMethods = null;
      m_classMethodMap = null;
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
    ITestNGMethod[] testConfigurationMethods = getBeforeTestConfigurationMethods();
    invokeTestConfigurations(testConfigurationMethods);
  }

  private void invokeTestConfigurations(ITestNGMethod[] testConfigurationMethods) {
    if (null != testConfigurationMethods && testConfigurationMethods.length > 0) {
      ConfigMethodArguments arguments =
          new Builder()
              .usingConfigMethodsAs(testConfigurationMethods)
              .forSuite(m_xmlTest.getSuite())
              .usingParameters(m_xmlTest.getAllParameters())
              .build();
      m_invoker.getConfigInvoker().invokeConfigurations(arguments);
    }
  }

  private ITestNGMethod[] m_allJunitTestMethods = new ITestNGMethod[] {};

  private void privateRunJUnit() {
    final ClassInfoMap cim = new ClassInfoMap(m_testClassesFromXml, false);
    final Set<Class<?>> classes = cim.getClasses();
    final List<ITestNGMethod> runMethods = Lists.newArrayList();
    List<IWorker<ITestNGMethod>> workers = Lists.newArrayList();
    // FIXME: directly referencing JUnitTestRunner which uses JUnit classes
    // may result in an class resolution exception under different JVMs
    // The resolution process is not specified in the JVM spec with a specific implementation,
    // so it can be eager => failure
    workers.add(
        new IWorker<ITestNGMethod>() {
          /** @see TestMethodWorker#getTimeOut() */
          @Override
          public long getTimeOut() {
            return 0;
          }

          /** @see java.lang.Runnable#run() */
          @Override
          public void run() {
            for (Class<?> tc : classes) {
              List<XmlInclude> includedMethods = cim.getXmlClass(tc).getIncludedMethods();
              List<String> methods = Lists.newArrayList();
              for (XmlInclude inc : includedMethods) {
                methods.add(inc.getName());
              }
              IJUnitTestRunner tr =
                  IJUnitTestRunner.createTestRunner(m_objectFactory, TestRunner.this);
              tr.setInvokedMethodListeners(m_invokedMethodListeners);
              try {
                tr.run(tc, methods.toArray(new String[0]));
              } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
              } finally {
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
            if (m_allJunitTestMethods.length == 1) {
              return m_allJunitTestMethods[0].getPriority();
            } else {
              return 0;
            }
          }

          @Override
          public int compareTo(@Nonnull IWorker<ITestNGMethod> other) {
            return getPriority() - other.getPriority();
          }
        });

    runJUnitWorkers(workers);
    m_allJunitTestMethods = runMethods.toArray(new ITestNGMethod[0]);
  }

  private static Comparator<ITestNGMethod> newComparator(boolean needPrioritySort) {
    return needPrioritySort ? new TestMethodComparator() : null;
  }

  private boolean sortOnPriority(ITestNGMethod[] interceptedOrder) {
    return m_methodInterceptors.size() > 1
        || Arrays.stream(interceptedOrder).anyMatch(m -> m.getPriority() != 0);
  }

  // If any of the test methods specify a priority other than the default, we'll need to be able to
  // sort them.
  private static BlockingQueue<Runnable> newQueue(boolean needPrioritySort) {
    return needPrioritySort ? new PriorityBlockingQueue<>() : new LinkedBlockingQueue<>();
  }

  /**
   * Main method that create a graph of methods and then pass it to the graph executor to run them.
   */
  private void privateRun(XmlTest xmlTest) {
    boolean parallel = xmlTest.getParallel().isParallel();

    // parallel
    int threadCount = parallel ? xmlTest.getThreadCount() : 1;
    // Make sure we create a graph based on the intercepted methods, otherwise an interceptor
    // removing methods would cause the graph never to terminate (because it would expect
    // termination from methods that never get invoked).
    ITestNGMethod[] interceptedOrder = intercept(getAllTestMethods());
    AtomicReference<IDynamicGraph<ITestNGMethod>> reference = new AtomicReference<>();
    TimeUtils.computeAndShowTime(
        "DynamicGraphHelper.createDynamicGraph()",
        () -> {
          IDynamicGraph<ITestNGMethod> ref =
              DynamicGraphHelper.createDynamicGraph(interceptedOrder, getCurrentXmlTest());
          reference.set(ref);
        });
    IDynamicGraph<ITestNGMethod> graph = reference.get();

    graph.setVisualisers(this.visualisers);
    // In some cases, additional sorting is needed to make sure tests run in the appropriate order.
    // If the user specified a method interceptor, or if we have any methods that have a non-default
    // priority on them, we need to sort.
    boolean needPrioritySort = sortOnPriority(interceptedOrder);
    Comparator<ITestNGMethod> methodComparator = newComparator(needPrioritySort);
    if (parallel) {
      if (graph.getNodeCount() <= 0) {
        return;
      }
      ITestNGThreadPoolExecutor executor =
          this.m_configuration
              .getExecutorFactory()
              .newTestMethodExecutor(
                  "test=" + xmlTest.getName(),
                  graph,
                  this,
                  threadCount,
                  threadCount,
                  0,
                  TimeUnit.MILLISECONDS,
                  newQueue(needPrioritySort),
                  methodComparator);
      executor.run();
      try {
        long timeOut = m_xmlTest.getTimeOut(XmlTest.DEFAULT_TIMEOUT_MS);
        Utils.log(
            "TestRunner",
            2,
            "Starting executor for test "
                + m_xmlTest.getName()
                + " with time out:"
                + timeOut
                + " milliseconds.");
        executor.awaitTermination(timeOut, TimeUnit.MILLISECONDS);
        executor.shutdownNow();
      } catch (InterruptedException handled) {
        LOGGER.error(handled.getMessage(), handled);
        Thread.currentThread().interrupt();
      }
      return;
    }
    List<ITestNGMethod> freeNodes = graph.getFreeNodes();

    if (graph.getNodeCount() > 0 && freeNodes.isEmpty()) {
      throw new TestNGException("No free nodes found in:" + graph);
    }

    while (!freeNodes.isEmpty()) {
      if (needPrioritySort) {
        freeNodes.sort(methodComparator);
        // Since this is sequential, let's run one at a time and fetch/sort freeNodes after each
        // method.
        // Future task: To optimize this, we can only update freeNodes after running a test that
        // another test is dependent upon.
        freeNodes = freeNodes.subList(0, 1);
      }
      createWorkers(freeNodes).forEach(Runnable::run);
      graph.setStatus(freeNodes, IDynamicGraph.Status.FINISHED);
      freeNodes = graph.getFreeNodes();
    }
  }

  /** Apply the method interceptor (if applicable) to the list of methods. */
  private ITestNGMethod[] intercept(ITestNGMethod[] methods) {

    List<IMethodInstance> methodInstances =
        MethodHelper.methodsToMethodInstances(Arrays.asList(methods));

    for (IMethodInterceptor m_methodInterceptor : m_methodInterceptors) {
      methodInstances = m_methodInterceptor.intercept(methodInstances, this);
    }

    List<ITestNGMethod> result = MethodHelper.methodInstancesToMethods(methodInstances);

    // Since an interceptor is involved, we would need to ensure that the ClassMethodMap object is
    // in sync with the
    // output of the interceptor, else @AfterClass doesn't get executed at all when interceptors are
    // involved.
    // so let's update the current classMethodMap object with the list of methods obtained from the
    // interceptor.
    this.m_classMethodMap = new ClassMethodMap(result, null);

    ITestNGMethod[] resultArray = result.toArray(new ITestNGMethod[0]);

    // Check if an interceptor had altered the effective test method count. If yes, then we need to
    // update our configurationGroupMethod object with that information.
    if (resultArray.length != testMethodsContainer.getItems().length) {
      m_groupMethods =
          new ConfigurationGroupMethods(
              new TestMethodContainer(() -> resultArray),
              m_groupMethods.getBeforeGroupsMethods(),
              m_groupMethods.getAfterGroupsMethods());
    }

    // If the user specified a method interceptor, whatever that returns is the order we're going
    // to run things in. Set the intercepted priority for that case.
    // There's a built-in interceptor, so look for more than one.
    if (m_methodInterceptors.size() > 1) {
      for (int i = 0; i < resultArray.length; ++i) {
        resultArray[i].setInterceptedPriority(i);
      }
    }

    return resultArray;
  }

  /**
   * Create a list of workers to run the methods passed in parameter. Each test method is run in its
   * own worker except in the following cases: - The method belongs to a class that
   * has @Test(sequential=true) - The parallel attribute is set to "classes" In both these cases,
   * all the methods belonging to that class will then be put in the same worker in order to run in
   * the same thread.
   */
  @Override
  public List<IWorker<ITestNGMethod>> createWorkers(List<ITestNGMethod> methods) {
    AbstractParallelWorker.Arguments args =
        new AbstractParallelWorker.Arguments.Builder()
            .classMethodMap(this.m_classMethodMap)
            .configMethods(this.m_groupMethods)
            .finder(this.m_annotationFinder)
            .invoker(this.m_invoker)
            .methods(methods)
            .testContext(this)
            .listeners(this.m_classListeners.values())
            .build();
    return AbstractParallelWorker.newWorker(
            m_xmlTest.getParallel(), m_xmlTest.getGroupByInstances())
        .createWorkers(args);
  }

  //
  // Invoke the workers
  //
  private void runJUnitWorkers(List<? extends IWorker<ITestNGMethod>> workers) {
    // Sequential run
    workers.forEach(Runnable::run);
  }

  private void afterRun() {
    // invoke @AfterTest
    ITestNGMethod[] testConfigurationMethods = getAfterTestConfigurationMethods();
    invokeTestConfigurations(testConfigurationMethods);

    //
    // Log the end date
    //
    m_endDate = new Date(System.currentTimeMillis());

    dumpInvokedMethods();

    // Invoke listeners
    fireEvent(false /*stop*/);
    removeAttribute(IObjectDispenser.GUICE_HELPER);
  }

  /** Logs the beginning of the {@link #beforeRun()} . */
  private void logStart() {
    log(
        "Running test "
            + m_testName
            + " on "
            + m_classMap.size()
            + " "
            + " classes, "
            + " included groups:["
            + Strings.valueOf(m_xmlMethodSelector.getIncludedGroups())
            + "] excluded groups:["
            + Strings.valueOf(m_xmlMethodSelector.getExcludedGroups())
            + "]");

    if (getVerbose() >= 3) {
      for (ITestClass tc : m_classMap.values()) {
        ((TestClass) tc).dump();
      }
    }
  }

  /**
   * Trigger the start/finish event.
   *
   * @param isStart <tt>true</tt> if the event is for start, <tt>false</tt> if the event is for
   *     finish
   */
  private void fireEvent(boolean isStart) {
    if (isStart) {
      for (ITestListener itl : m_testListeners) {
        itl.onStart(this);
      }

    } else {
      List<ITestListener> testListenersReversed = Lists.newReversedArrayList(m_testListeners);
      for (ITestListener itl : testListenersReversed) {
        itl.onFinish(this);
      }
    }
    if (!isStart) {
      MethodHelper.clear(methods(this.getPassedConfigurations()));
      MethodHelper.clear(methods(this.getFailedConfigurations()));
      MethodHelper.clear(methods(this.getSkippedConfigurations()));
      MethodHelper.clear(methods(Arrays.stream(this.getAllTestMethods())));
    }
  }

  private static Stream<Method> methods(IResultMap resultMap) {
    return methods(resultMap.getAllMethods().stream());
  }

  private static Stream<Method> methods(Stream<ITestNGMethod> methods) {
    return methods.map(each -> each.getConstructorOrMethod().getMethod());
  }

  /////
  // ITestContext
  //
  @Override
  public String getName() {
    return m_testName;
  }

  /** @return Returns the startDate. */
  @Override
  public Date getStartDate() {
    return m_startDate;
  }

  /** @return Returns the endDate. */
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
    Map<String, String> ig = m_xmlMethodSelector.getIncludedGroups();
    return ig.values().toArray(new String[0]);
  }

  @Override
  public String[] getExcludedGroups() {
    Map<String, String> eg = m_xmlMethodSelector.getExcludedGroups();
    return eg.values().toArray(new String[0]);
  }

  @Override
  public String getOutputDirectory() {
    return m_outputDirectory;
  }

  /** @return Returns the suite. */
  @Override
  public ISuite getSuite() {
    return m_suite;
  }

  @Override
  public ITestNGMethod[] getAllTestMethods() {
    if (getTest().isJUnit()) {
      // This is true only when we are running JUnit mode
      return m_allJunitTestMethods;
    }
    return testMethodsContainer.getItems();
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

  /** @see org.testng.ITestContext#getFailedConfigurations() */
  @Override
  public IResultMap getFailedConfigurations() {
    return m_failedConfigurations;
  }

  @Override
  public IResultMap getConfigurationsScheduledForInvocation() {
    return m_configsToBeInvoked;
  }

  /** @see org.testng.ITestContext#getPassedConfigurations() */
  @Override
  public IResultMap getPassedConfigurations() {
    return m_passedConfigurations;
  }

  /** @see org.testng.ITestContext#getSkippedConfigurations() */
  @Override
  public IResultMap getSkippedConfigurations() {
    return m_skippedConfigurations;
  }

  @Override
  public void addPassedTest(ITestNGMethod tm, ITestResult tr) {
    m_passedTests.addResult(tr);
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
    m_skippedTests.addResult(tr);
  }

  @Override
  public void addFailedTest(ITestNGMethod testMethod, ITestResult result) {
    logFailedTest(result, false /* withinSuccessPercentage */);
  }

  @Override
  public void addFailedButWithinSuccessPercentageTest(
      ITestNGMethod testMethod, ITestResult result) {
    logFailedTest(result, true /* withinSuccessPercentage */);
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
    List<IConfigurationListener> listeners = Lists.newArrayList(m_configurationListeners);
    for (IConfigurationListener each : this.m_configuration.getConfigurationListeners()) {
      boolean duplicate = false;
      for (IConfigurationListener listener : listeners) {
        if (each.getClass().equals(listener.getClass())) {
          duplicate = true;
          break;
        }
      }
      if (!duplicate) {
        listeners.add(each);
      }
    }
    return Lists.newArrayList(listeners);
  }

  private void logFailedTest(ITestResult tr, boolean withinSuccessPercentage) {
    if (withinSuccessPercentage) {
      m_failedButWithinSuccessPercentageTests.addResult(tr);
    } else {
      m_failedTests.addResult(tr);
    }
  }

  private static void log(String s) {
    Utils.log("TestRunner", 3, s);
  }

  public static int getVerbose() {
    return Utils.getVerbose();
  }

  // TODO: instance method adjusts static configuration. Should it be removed?
  public void setVerbose(int n) {
    Utils.setVerbose(n);
  }

  // TODO: This method needs to be removed and we need to be leveraging addListener().
  // Investigate and fix this.
  void addTestListener(ITestListener listener) {
    Optional<ITestListener> found =
        m_testListeners.stream()
            .filter(iTestListener -> iTestListener.getClass().equals(listener.getClass()))
            .findAny();
    if (found.isPresent()) {
      return;
    }
    m_testListeners.add(listener);
  }

  public void addListener(ITestNGListener listener) {
    // TODO a listener may be added many times if it implements many interfaces
    if (listener instanceof IMethodInterceptor) {
      m_methodInterceptors.add((IMethodInterceptor) listener);
    }
    if (listener instanceof ITestListener) {
      // At this point, the field m_testListeners has already been used in the creation
      addTestListener((ITestListener) listener);
    }
    if (listener instanceof IClassListener) {
      IClassListener classListener = (IClassListener) listener;
      if (!m_classListeners.containsKey(classListener.getClass())) {
        m_classListeners.put(classListener.getClass(), classListener);
      }
    }
    if (listener instanceof IConfigurationListener) {
      addConfigurationListener((IConfigurationListener) listener);
    }
    if (listener instanceof IConfigurable) {
      m_configuration.setConfigurable((IConfigurable) listener);
    }
    if (listener instanceof IHookable) {
      m_configuration.setHookable((IHookable) listener);
    }
    if (listener instanceof IExecutionListener) {
      IExecutionListener iel = (IExecutionListener) listener;
      if (m_configuration.addExecutionListenerIfAbsent(iel)) {
        iel.onExecutionStart();
      }
    }
    if (listener instanceof IDataProviderListener) {
      IDataProviderListener dataProviderListener = (IDataProviderListener) listener;
      holder.addListener(dataProviderListener);
    }
    if (listener instanceof IDataProviderInterceptor) {
      IDataProviderInterceptor interceptor = (IDataProviderInterceptor) listener;
      holder.addInterceptor(interceptor);
    }

    if (listener instanceof IExecutionVisualiser) {
      IExecutionVisualiser l = (IExecutionVisualiser) listener;
      visualisers.add(l);
    }
    m_suite.addListener(listener);
  }

  void addConfigurationListener(IConfigurationListener icl) {
    m_configurationListeners.add(icl);
  }

  private void dumpInvokedMethods() {
    MethodHelper.dumpInvokedMethodInfoToConsole(getAllTestMethods(), getVerbose());
  }

  private final IResultMap m_passedConfigurations = new ResultMap();
  private final IResultMap m_skippedConfigurations = new ResultMap();
  private final IResultMap m_failedConfigurations = new ResultMap();
  private final IResultMap m_configsToBeInvoked = new ResultMap();

  private class ConfigurationListener implements IConfigurationListener {
    @Override
    public void beforeConfiguration(ITestResult tr) {
      m_configsToBeInvoked.addResult(tr);
    }

    @Override
    public void onConfigurationFailure(ITestResult itr) {
      m_failedConfigurations.addResult(itr);
      removeConfigurationResultAfterExecution(itr);
    }

    @Override
    public void onConfigurationSkip(ITestResult itr) {
      m_skippedConfigurations.addResult(itr);
      removeConfigurationResultAfterExecution(itr);
    }

    @Override
    public void onConfigurationSuccess(ITestResult itr) {
      m_passedConfigurations.addResult(itr);
      removeConfigurationResultAfterExecution(itr);
    }

    private void removeConfigurationResultAfterExecution(ITestResult itr) {
      // The remove method of ResultMap removes based on hashCode
      // So lets find the result based on the method and remove it off.
      m_configsToBeInvoked.getAllResults().removeIf(tr -> tr.getMethod().equals(itr.getMethod()));
    }
  }

  void addMethodInterceptor(IMethodInterceptor methodInterceptor) {
    // avoid to add interceptor twice when the defined listeners implements both ITestListener and
    // IMethodInterceptor.
    if (!m_methodInterceptors.contains(methodInterceptor)) {
      m_methodInterceptors.add(methodInterceptor);
    }
  }

  @Override
  public XmlTest getCurrentXmlTest() {
    return m_xmlTest;
  }

  private final IAttributes m_attributes = new Attributes();

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

  @Override
  public IInjectorFactory getInjectorFactory() {
    return this.m_injectorFactory;
  }
}
