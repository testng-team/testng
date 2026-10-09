package org.testng;

import static org.testng.ListenerComparator.sort;
import static org.testng.internal.MethodHelper.fixMethodsWithClass;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.testng.internal.Attributes;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.ClassBasedWrapper;
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
import org.testng.internal.ListenerOrderDeterminer;
import org.testng.internal.MethodGroupsHelper;
import org.testng.internal.MethodHelper;
import org.testng.internal.MethodSorting;
import org.testng.internal.ParameterResolverHolder;
import org.testng.internal.ResultMap;
import org.testng.internal.RunInfo;
import org.testng.internal.RuntimeBehavior;
import org.testng.internal.TestListenerHelper;
import org.testng.internal.TestMethodComparator;
import org.testng.internal.TestMethodContainer;
import org.testng.internal.TestNGClassFinder;
import org.testng.internal.TestNGMethodFinder;
import org.testng.internal.TestResult;
import org.testng.internal.Utils;
import org.testng.internal.XmlMethodSelector;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.invokers.AbstractParallelWorker;
import org.testng.internal.invokers.ConfigMethodArguments;
import org.testng.internal.invokers.IInvoker;
import org.testng.internal.invokers.Invoker;
import org.testng.internal.objects.IObjectDispenser;
import org.testng.internal.thread.ThreadTimeoutException;
import org.testng.thread.IThreadWorkerFactory;
import org.testng.thread.IWorker;
import org.testng.util.Strings;
import org.testng.util.TimeUtils;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlPackage;
import org.testng.xml.XmlTest;

/**
 * Runs one {@code <test>} of a suite.
 *
 * <p>The constructor finds the test classes, the test methods and the configuration methods of the
 * {@code <test>}. It also creates the listeners that {@code @Listeners} annotations name. {@link
 * #run()} then runs the methods and keeps their results.
 *
 * <p>The runner is also the {@link ITestContext} of the {@code <test>}, which listeners receive.
 */
public class TestRunner
    implements ITestContext,
        ITestResultNotifier,
        IThreadWorkerFactory<ITestNGMethod>,
        IConfigEavesdropper {

  private static final String DEFAULT_PROP_OUTPUT_DIR = "test-output";

  private final Comparator<ITestNGMethod> comparator;
  private ISuite m_suite;
  private XmlTest m_xmlTest;
  private String m_testName;
  private IInjectorFactory m_injectorFactory;
  private ITestObjectFactory m_objectFactory;

  private List<XmlClass> m_testClassesFromXml;

  private IInvoker m_invoker;
  private IAnnotationFinder m_annotationFinder;

  /** The test listeners of this {@code <test>}. */
  private final List<ITestListener> m_testListeners = new ArrayList<>();

  /**
   * The configuration listeners. This is a list and not a set. The order of the list decides the
   * order in which TestNG calls the listeners.
   *
   * <p>{@link #insertConfigurationListener} does not add a second listener of the same class. A set
   * would keep out only an equal object, which is a weaker check. {@link
   * #getConfigurationListeners()} tries the class check again, through {@link ClassBasedWrapper}.
   * That second check can miss a duplicate, but only {@link #insertConfigurationListener} adds to
   * this list.
   */
  private final List<IConfigurationListener> m_configurationListeners = new ArrayList<>();

  private final Set<IExecutionVisualiser> visualisers = new HashSet<>();

  private final IConfigurationListener m_confListener = new ConfigurationListener();

  private final Map<Class<? extends IClassListener>, IClassListener> m_classListeners =
      new LinkedHashMap<>();
  private final DataProviderHolder holder;

  /**
   * The parameter resolvers. This runner shares the holder of its {@link SuiteRunner}, and does not
   * copy it. So this runner also sees a resolver that the suite gets after this runner exists. When
   * the suite is not a {@code SuiteRunner}, the runner gets a holder of its own.
   */
  private ParameterResolverHolder parameterResolverHolder;

  private Instant m_startInstant = Instant.now();
  private @Nullable Instant m_endInstant = null;
  private final IContainer<ITestNGMethod> testMethodsContainer =
      new TestMethodContainer(this::computeAndGetAllTestMethods);

  /** Maps each Java class to its test class. */
  private final Map<Class<?>, ITestClass> m_classMap = new LinkedHashMap<>();

  /** The directory for the reports. */
  private String m_outputDirectory = DEFAULT_PROP_OUTPUT_DIR;

  // Selects the methods by the groups and the methods that the XML includes and excludes.
  private final XmlMethodSelector m_xmlMethodSelector = new XmlMethodSelector();

  private @Nullable ITestListener exitCodeListener;

  //
  // The @BeforeSuite, @AfterSuite, @BeforeTest and @AfterTest methods of the test classes of this
  // <test>. initMethods() fills them with the methods to run, in the order in which they run. It
  // adds the methods that it leaves out to the list that getExcludedMethods() returns.
  //
  private ITestNGMethod[] m_beforeSuiteMethods = {};

  private ITestNGMethod[] m_afterSuiteMethods = {};
  private ITestNGMethod[] m_beforeXmlTestMethods = {};
  private ITestNGMethod[] m_afterXmlTestMethods = {};
  private final List<ITestNGMethod> m_excludedMethods = new ArrayList<>();
  private @Nullable ConfigurationGroupMethods m_groupMethods = null;

  // The groups that the <define> tags of the <test> make from other groups.
  private final Map<String, List<String>> m_metaGroups = new HashMap<>();

  // The results of the test methods, one map for each status.
  private final IResultMap m_passedTests = new ResultMap();
  private final IResultMap m_failedTests = new ResultMap();
  private final IResultMap m_failedButWithinSuccessPercentageTests = new ResultMap();
  private final IResultMap m_skippedTests = new ResultMap();

  private final Object resultLock = new Object();
  private final IdentityHashMap<ITestNGMethod, Integer> inFlightInvocations =
      new IdentityHashMap<>();
  private final Set<ITestResult> admittedResults =
      Collections.newSetFromMap(new IdentityHashMap<>());
  private volatile boolean resultsFrozen;
  private volatile boolean hasInterceptedMethods;
  private volatile ITestNGMethod[] interceptedTestMethods = new ITestNGMethod[0];

  private final RunInfo m_runInfo = new RunInfo(this::getCurrentXmlTest);

  // The host that runs this <test>, or null when the run is local.
  private @Nullable String m_host;

  // The method interceptors. init() puts a built-in interceptor first. The preserve-order
  // attribute of the <test> decides which one. The interceptors of the user come after it.
  private final List<IMethodInterceptor> m_methodInterceptors = new ArrayList<>();

  private @Nullable ClassMethodMap m_classMethodMap;
  private @Nullable TestNGClassFinder m_testClassFinder;
  private IConfiguration m_configuration;

  /**
   * The kinds of order rule between two test methods, from the weakest to the strongest.
   *
   * <p>The method graph uses the ordinal of each kind as the weight of an edge. The graph can reach
   * a state where no method is free to run and no method is running. Then it frees the methods
   * whose edges all have the lowest weight. So when the rules make a cycle, the graph ignores the
   * weaker rule first.
   */
  public enum PriorityWeight {
    /**
     * The order that {@code group-by-instances} adds, so that the methods of one instance run
     * together.
     */
    groupByInstance,
    /**
     * The order that {@code preserve-order} adds, so that the classes run in the order of the XML.
     */
    preserveOrder,
    /** TestNG adds no edge of this kind. */
    priority,
    /** A dependency on the methods of a group, for example from {@code dependsOnGroups}. */
    dependsOnGroups,
    /** A dependency on a method, from {@code dependsOnMethods}. */
    dependsOnMethods
  }

  /**
   * Creates a runner for {@code test}.
   *
   * @param configuration the configuration of the run.
   * @param suite the {@link ISuite} that stands for the {@code <suite>}.
   * @param test the {@link XmlTest} that stands for the {@code <test>}.
   * @param outputDirectory the directory for the reports.
   * @param finder the finder that reads the annotations.
   * @param skipFailedInvocationCounts whether to skip the remaining invocations of a test method
   *     after one invocation fails.
   * @param invokedMethodListeners the {@link IInvokedMethodListener} listeners.
   * @param classListeners the {@link IClassListener} listeners.
   * @param comparator the comparator that orders the test methods.
   * @param otherHolder the data provider listeners and interceptors.
   * @param suiteRunner the runner of the suite, which this runner calls back.
   */
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
      DataProviderHolder otherHolder,
      ISuiteRunnerListener suiteRunner) {
    this.comparator = comparator;
    this.holder = otherHolder;
    init(
        configuration,
        suite,
        test,
        outputDirectory,
        finder,
        skipFailedInvocationCounts,
        invokedMethodListeners,
        classListeners,
        suiteRunner);
  }

  /**
   * Creates a runner for {@code test}. The output directory and the annotation finder come from
   * {@code suite}.
   *
   * @param configuration the configuration of the run.
   * @param suite the {@link ISuite} that stands for the {@code <suite>}.
   * @param test the {@link XmlTest} that stands for the {@code <test>}.
   * @param skipFailedInvocationCounts whether to skip the remaining invocations of a test method
   *     after one invocation fails.
   * @param invokedMethodListeners the {@link IInvokedMethodListener} listeners.
   * @param classListeners the {@link IClassListener} listeners.
   * @param comparator the comparator that orders the test methods.
   * @param suiteRunner the runner of the suite, which this runner calls back.
   */
  public TestRunner(
      IConfiguration configuration,
      ISuite suite,
      XmlTest test,
      boolean skipFailedInvocationCounts,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      List<IClassListener> classListeners,
      Comparator<ITestNGMethod> comparator,
      ISuiteRunnerListener suiteRunner) {
    this.comparator = comparator;
    this.holder = new DataProviderHolder(configuration);
    init(
        configuration,
        suite,
        test,
        suite.getOutputDirectory(),
        suite.getAnnotationFinder(),
        skipFailedInvocationCounts,
        invokedMethodListeners,
        classListeners,
        suiteRunner);
  }

  /**
   * Creates a runner for {@code test}. The output directory and the annotation finder come from
   * {@code suite}. The {@code testng.order} system property sets the order of the test methods.
   *
   * @param configuration the configuration of the run.
   * @param suite the {@link ISuite} that stands for the {@code <suite>}.
   * @param test the {@link XmlTest} that stands for the {@code <test>}.
   * @param skipFailedInvocationCounts whether to skip the remaining invocations of a test method
   *     after one invocation fails.
   * @param invokedMethodListeners the {@link IInvokedMethodListener} listeners.
   * @param classListeners the {@link IClassListener} listeners.
   * @param suiteRunner the runner of the suite, which this runner calls back.
   */
  /* /!\ testng-remote calls this constructor. Talk to the TestNG team before you change it. */
  public TestRunner(
      IConfiguration configuration,
      ISuite suite,
      XmlTest test,
      boolean skipFailedInvocationCounts,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      List<IClassListener> classListeners,
      ISuiteRunnerListener suiteRunner) {
    this.comparator = MethodSorting.basedOn();
    this.holder = new DataProviderHolder(configuration);
    init(
        configuration,
        suite,
        test,
        suite.getOutputDirectory(),
        suite.getAnnotationFinder(),
        skipFailedInvocationCounts,
        invokedMethodListeners,
        classListeners,
        suiteRunner);
  }

  private void init(
      IConfiguration configuration,
      ISuite suite,
      XmlTest test,
      String outputDirectory,
      IAnnotationFinder annotationFinder,
      boolean skipFailedInvocationCounts,
      Collection<IInvokedMethodListener> invokedMethodListeners,
      List<IClassListener> classListeners,
      ISuiteRunnerListener suiteRunner) {
    m_configuration = configuration;
    m_xmlTest = test;
    m_suite = suite;
    parameterResolverHolder =
        suite instanceof SuiteRunner
            ? ((SuiteRunner) suite).getParameterResolverHolder()
            : new ParameterResolverHolder(configuration);
    m_testName = test.getName();
    m_host = suite.getHost();
    m_testClassesFromXml = test.getXmlClasses();
    m_injectorFactory = m_configuration.getInjectorFactory();
    m_objectFactory =
        Objects.requireNonNull(
            suite.getObjectFactory(), "a running suite carries an object factory");
    setVerbose(test.getVerbose());
    if (suiteRunner == null) {
      if (suite instanceof ISuiteRunnerListener) {
        setExitCodeListener(((ISuiteRunnerListener) suite).getExitCodeListener());
      }
    } else {
      setExitCodeListener(suiteRunner.getExitCodeListener());
    }

    boolean preserveOrder = test.getPreserveOrder();
    IMethodInterceptor builtinInterceptor =
        preserveOrder
            ? new PreserveOrderMethodInterceptor()
            : new InstanceOrderingMethodInterceptor();
    m_methodInterceptors.clear();
    // Put the built-in interceptor first. The interceptors of the user run after it, so they
    // decide the final order. A ListenerComparator can change this order.
    m_methodInterceptors.add(builtinInterceptor);

    List<XmlPackage> m_packageNamesFromXml = getAllPackages();
    if (!m_packageNamesFromXml.isEmpty()) {
      // Add the classes of each <package>, except a class that the <test> already names. A class
      // from a <package> is not a second copy of a class that a <class> tag names. The check also
      // protects a second run. This loop adds to the list of the XmlTest itself. Without the check,
      // each run over the same XmlTest would add the classes again.
      Set<String> named =
          m_testClassesFromXml.stream().map(XmlClass::getName).collect(Collectors.toSet());
      for (XmlPackage xp : m_packageNamesFromXml) {
        for (XmlClass scanned : xp.getXmlClasses()) {
          if (named.add(scanned.getName())) {
            m_testClassesFromXml.add(scanned);
          }
        }
      }
    }

    // A <class> finds the parameters of its <test> through a link to the <test>. Of the parsers,
    // only the XML parser sets that link. Set it here, because every suite comes through here,
    // whatever built it: XML, YAML, a <package> scan or the Java API. Do not set the link from an
    // <include> to its <class>. Two XmlClass copies can share one XmlInclude, so one link cannot be
    // right for both. Each scheduled method keeps both of its tags instead.
    for (XmlClass xmlClass : m_testClassesFromXml) {
      xmlClass.setXmlTest(m_xmlTest);
    }

    m_annotationFinder = annotationFinder;
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
            holder,
            parameterResolverHolder,
            m_confListener,
            suiteRunner);

    if (test.getParallel() != null) {
      log("Running the tests in '" + test.getName() + "' with parallel mode:" + test.getParallel());
    }

    setOutputDirectory(outputDirectory);

    // Read the groups and the method selectors, then find the methods and the listeners.
    init();
  }

  /**
   * Returns the {@code <package>} tags of the suite, then the {@code <package>} tags of this {@code
   * <test>}. Never returns {@code null}.
   */
  private List<XmlPackage> getAllPackages() {
    final List<XmlPackage> allPackages = new ArrayList<>();
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

  /**
   * Returns the invoker that runs the configuration methods and the test methods of this {@code
   * <test>}.
   *
   * @return the invoker.
   */
  public IInvoker getInvoker() {
    return m_invoker;
  }

  /**
   * Returns the {@code @BeforeSuite} methods of the test classes of this {@code <test>}, in the
   * order in which they run. The {@link SuiteRunner} collects them from each {@code <test>}, and
   * runs them.
   *
   * @return the {@code @BeforeSuite} methods.
   */
  public ITestNGMethod[] getBeforeSuiteMethods() {
    return m_beforeSuiteMethods;
  }

  /**
   * Returns the {@code @AfterSuite} methods of the test classes of this {@code <test>}, in the
   * order in which they run. The {@link SuiteRunner} collects them from each {@code <test>}, and
   * runs them.
   *
   * @return the {@code @AfterSuite} methods.
   */
  public ITestNGMethod[] getAfterSuiteMethods() {
    return m_afterSuiteMethods;
  }

  /**
   * Returns the {@code @BeforeTest} methods of the test classes of this {@code <test>}, in the
   * order in which they run.
   *
   * @return the {@code @BeforeTest} methods.
   */
  public ITestNGMethod[] getBeforeTestConfigurationMethods() {
    return m_beforeXmlTestMethods;
  }

  /**
   * Returns the {@code @AfterTest} methods of the test classes of this {@code <test>}, in the order
   * in which they run.
   *
   * @return the {@code @AfterTest} methods.
   */
  public ITestNGMethod[] getAfterTestConfigurationMethods() {
    return m_afterXmlTestMethods;
  }

  private void init() {
    initMetaGroups(m_xmlTest);
    initRunInfo(m_xmlTest);

    // Find the test classes and their methods.
    initMethods();

    initListeners();
    for (IConfigurationListener cl : m_configuration.getConfigurationListeners()) {
      addConfigurationListener(cl);
    }
  }

  private void initListeners() {
    //
    // Collect the listener classes that the @Listeners annotations of the test classes name. A
    // listener class that implements ITestNGListenerFactory is also the listener factory.
    //
    Set<Class<? extends ITestNGListener>> listenerClasses = new LinkedHashSet<>();
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

    //
    // Choose the factory that creates the listeners. The listener factory from @Listeners comes
    // first, then the factory of the configuration, then the default factory.
    //

    ITestNGListenerFactory factory;
    if (listenerFactoryClass != null) {
      factory = m_objectFactory.newInstance(listenerFactoryClass);
    } else if (m_configuration.getListenerFactory() != null) {
      factory = m_configuration.getListenerFactory();
    } else {
      factory = new DefaultListenerFactory(m_objectFactory, this);
    }

    // Create each listener, and add it.
    for (Class<? extends ITestNGListener> c : listenerClasses) {
      ITestNGListener created = factory.createListener(c);
      if (created != null) {
        addListener(created);
      }
    }
  }

  /** Reads the groups that the {@code <define>} tags of the {@code <test>} make. */
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

    // Whether the group rules also apply to a method that an <include> names
    m_xmlMethodSelector.setOverrideIncludedMethods(m_configuration.getOverrideIncludedMethods());

    // Methods
    m_xmlMethodSelector.setXmlClasses(m_xmlTest.getXmlClasses());

    m_runInfo.addMethodSelector(m_xmlMethodSelector, 10);

    // Add the method selectors that the <test> names by class. m_xmlMethodSelector already has a
    // script from setScript(). That is the script of the first selector only.
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
    // Find the methods to run
    //
    List<ITestNGMethod> beforeClassMethods = new ArrayList<>();
    List<ITestNGMethod> testMethods = new ArrayList<>();
    List<ITestNGMethod> afterClassMethods = new ArrayList<>();
    List<ITestNGMethod> beforeSuiteMethods = new ArrayList<>();
    List<ITestNGMethod> afterSuiteMethods = new ArrayList<>();
    List<ITestNGMethod> beforeXmlTestMethods = new ArrayList<>();
    List<ITestNGMethod> afterXmlTestMethods = new ArrayList<>();

    ClassInfoMap classMap = new ClassInfoMap(m_testClassesFromXml);
    m_testClassFinder =
        new TestNGClassFinder(classMap, new HashMap<>(), m_configuration, this, holder);
    ITestMethodFinder testMethodFinder =
        new TestNGMethodFinder(m_objectFactory, m_runInfo, m_annotationFinder, comparator);

    //
    // Create a TestClass for each class. At this point, each TestClass finds only its test methods.
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
              classMap.getXmlClasses(ic.getRealClass()),
              m_testClassFinder.getFactoryCreationFailedMessage());
      m_classMap.put(ic.getRealClass(), tc);
    }

    // Bind each test method to its test class, and give the test methods to the method selectors.
    // Then let each TestClass find its configuration methods, which the selectors filter.
    for (ITestClass tc : m_classMap.values()) {
      fixMethodsWithClass(tc.getTestMethods(), tc, testMethods);
    }
    m_runInfo.setTestMethods(testMethods);
    for (ITestClass tc : m_classMap.values()) {
      if (tc instanceof TestClass) {
        ((TestClass) tc).initConfigurationMethods();
      }
    }

    //
    // Find the @BeforeGroups and @AfterGroups methods, by group
    //
    Map<String, List<ITestNGMethod>> beforeGroupMethods =
        MethodGroupsHelper.findGroupsMethods(m_classMap.values(), true);
    Map<String, List<ITestNGMethod>> afterGroupMethods =
        MethodGroupsHelper.findGroupsMethods(m_classMap.values(), false);

    //
    // Bind each configuration method to its test class, and collect the methods of each kind
    //

    for (ITestClass tc : m_classMap.values()) {
      fixMethodsWithClass(beforeClassConfigMethods(tc), tc, beforeClassMethods);
      fixMethodsWithClass(tc.getBeforeTestMethods(), tc, null);
      fixMethodsWithClass(tc.getAfterTestMethods(), tc, null);
      fixMethodsWithClass(afterClassConfigMethods(tc), tc, afterClassMethods);
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
            true /* unique */,
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
            true /* unique */,
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

  private static ITestNGMethod[] beforeClassConfigMethods(ITestClass tc) {
    return ITestClassConfigInfo.allBeforeClassMethods(tc).toArray(ITestNGMethod[]::new);
  }

  private static ITestNGMethod[] afterClassConfigMethods(ITestClass tc) {
    return ITestClassConfigInfo.allAfterClassMethods(tc).toArray(ITestNGMethod[]::new);
  }

  private ITestNGMethod[] computeAndGetAllTestMethods() {
    List<ITestNGMethod> testMethods = new ArrayList<>();
    for (ITestClass tc : m_classMap.values()) {
      fixMethodsWithClass(tc.getTestMethods(), tc, testMethods);
    }

    return MethodHelper.collectAndOrderMethods(
        testMethods,
        true /* forTests */,
        m_runInfo,
        m_annotationFinder,
        false /* unique */,
        m_excludedMethods,
        comparator);
  }

  /**
   * Returns the test classes of this {@code <test>}.
   *
   * @return a view of the test classes, in the order in which the runner found them.
   */
  public Collection<ITestClass> getTestClasses() {
    return m_classMap.values();
  }

  /**
   * Changes the name that {@link #getName()} returns. The {@link XmlTest} keeps its own name.
   *
   * @param name the new name.
   */
  public void setTestName(String name) {
    m_testName = name;
  }

  /**
   * Sets the directory for the reports, which {@link #getOutputDirectory()} returns.
   *
   * @param od the directory.
   */
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
   * Runs this {@code <test>}.
   *
   * <p>This method does these steps in this order:
   *
   * <ol>
   *   <li>It calls {@code onStart} on each test listener.
   *   <li>It runs the {@code @BeforeTest} methods.
   *   <li>It runs the test methods, with their configuration methods.
   *   <li>It runs the {@code @AfterTest} methods.
   *   <li>It calls {@code onFinish} on each test listener.
   * </ol>
   *
   * <p>This method still does the last two steps when step 3 throws.
   */
  public void run() {
    beforeRun();

    try {
      XmlTest test = getTest();
      privateRun(test);
    } finally {
      afterRun();
      forgetHeavyReferencesIfNeeded();
    }
  }

  /**
   * Returns the {@link ClassMethodMap} of the run. {@link #forgetHeavyReferencesIfNeeded()} can
   * drop it when the run ends.
   */
  private ClassMethodMap requireClassMethodMap() {
    return Objects.requireNonNull(m_classMethodMap, "the run still holds its method map");
  }

  /**
   * Returns the {@link ConfigurationGroupMethods} of the run. {@link
   * #forgetHeavyReferencesIfNeeded()} can drop them when the run ends.
   */
  private ConfigurationGroupMethods requireGroupMethods() {
    return Objects.requireNonNull(m_groupMethods, "the run still holds its group methods");
  }

  private void forgetHeavyReferencesIfNeeded() {
    if (RuntimeBehavior.isMemoryFriendlyMode()) {
      testMethodsContainer.clearItems();
      m_groupMethods = null;
      m_classMethodMap = null;
    }
  }

  /** Calls {@code onStart} on each test listener, then runs the {@code @BeforeTest} methods. */
  private void beforeRun() {
    //
    // Record the start time
    //
    m_startInstant = Instant.now();

    // Log the start of the run
    logStart();

    // Call onStart on the test listeners
    fireEvent(true /*start*/);

    // Run the @BeforeTest methods
    ITestNGMethod[] testConfigurationMethods = getBeforeTestConfigurationMethods();
    invokeTestConfigurations(testConfigurationMethods);
  }

  private void invokeTestConfigurations(ITestNGMethod[] testConfigurationMethods) {
    if (null != testConfigurationMethods && testConfigurationMethods.length > 0) {
      ConfigMethodArguments arguments =
          new ConfigMethodArguments.Builder()
              .usingConfigMethodsAs(testConfigurationMethods)
              .forSuite(m_xmlTest.getSuite())
              .usingParameters(m_xmlTest.getAllParameters())
              .build();
      m_invoker.getConfigInvoker().invokeConfigurations(arguments);
    }
  }

  private static @Nullable Comparator<ITestNGMethod> newComparator(boolean needPrioritySort) {
    return needPrioritySort ? new TestMethodComparator() : null;
  }

  // A priority queue sorts the methods. Use it when a test method has a priority other than the
  // default.
  private static BlockingQueue<Runnable> newQueue(boolean needPrioritySort) {
    return needPrioritySort ? new PriorityBlockingQueue<>() : new LinkedBlockingQueue<>();
  }

  /**
   * Builds a graph of the test methods, and runs it.
   *
   * <p>In a parallel mode, a {@link TestTaskExecutor} runs the graph on a thread pool. Otherwise,
   * this method runs the methods that are free to run, one after another.
   */
  private void privateRun(XmlTest xmlTest) {
    boolean parallel = xmlTest.getParallel().isParallel();

    ITestNGMethod[] allMethods = getAllTestMethods();

    // Build the graph from the methods that the interceptors return. A graph of all the methods
    // would wait for ever for a method that an interceptor removed.
    ITestNGMethod[] interceptedOrder = intercept(allMethods);
    IDynamicGraph<ITestNGMethod> graph =
        TimeUtils.computeAndShowTime(
            "DynamicGraphHelper.createDynamicGraph()",
            () ->
                DynamicGraphHelper.createDynamicGraph(
                    interceptedOrder,
                    getCurrentXmlTest(),
                    requireGroupMethods().getBeforeGroupsMethods()));

    // Give each method the dependencies that the graph holds for it. With a user interceptor, do
    // this first for all the methods, not only for the ones that came back. intercept() gave each
    // method its declared dependencies. The graph has no node for a method that an interceptor
    // removed. So it returns an empty set, which replaces the declared dependencies. Then do the
    // same for the methods that came back, which can include a method that an interceptor added.
    // Without a user interceptor, skip the first call. Nothing set the dependencies before, and
    // both calls would cover the same methods.
    if (hasUserMethodInterceptors()) {
      publishDependencies(allMethods, graph);
    }
    publishDependencies(interceptedOrder, graph);

    Collection<IExecutionVisualiser> original =
        sort(this.visualisers, m_configuration.getListenerComparator());
    graph.setVisualisers(new LinkedHashSet<>(original));
    // Sort the free methods when a user interceptor ran, or when a method has a priority other
    // than the default.
    boolean hasNonZeroPriorityMethods =
        Arrays.stream(interceptedOrder).anyMatch(m -> m.getPriority() != 0);
    boolean needPrioritySort = hasUserMethodInterceptors() || hasNonZeroPriorityMethods;
    Comparator<ITestNGMethod> methodComparator = newComparator(needPrioritySort);
    if (parallel) {
      if (graph.getNodeCount() <= 0) {
        return;
      }
      TestTaskExecutor taskExecutor =
          new TestTaskExecutor(
              m_configuration,
              xmlTest,
              this,
              newQueue(hasNonZeroPriorityMethods),
              graph,
              methodComparator);
      taskExecutor.execute();
      taskExecutor.awaitCompletion();
      return;
    }
    List<ITestNGMethod> freeNodes = graph.getFreeNodes();

    if (graph.getNodeCount() > 0 && freeNodes.isEmpty()) {
      throw new TestNGException("No free nodes found in:" + graph);
    }

    while (!freeNodes.isEmpty()) {
      if (needPrioritySort) {
        freeNodes.sort(methodComparator);
        // The run is sequential. So run one method at a time, and get and sort the free methods
        // again after each one.
        // A possible speed-up: get the free methods again only after a method that another method
        // depends on.
        freeNodes = freeNodes.subList(0, 1);
      }
      createWorkers(freeNodes).forEach(Runnable::run);
      graph.setStatus(freeNodes, IDynamicGraph.Status.FINISHED);
      freeNodes = graph.getFreeNodes();
    }
  }

  /**
   * Gives each method the dependencies that the graph holds for it.
   *
   * <p>The setters are on {@link BaseTestMethod}, not on {@link ITestNGMethod}, so that users
   * cannot call them. That is why this method checks the type of each method.
   */
  private static void publishDependencies(
      ITestNGMethod[] methods, IDynamicGraph<ITestNGMethod> graph) {
    for (ITestNGMethod each : methods) {
      if (each instanceof BaseTestMethod) {
        Set<ITestNGMethod> downstream = new HashSet<>(graph.getDependenciesFor(each));
        ((BaseTestMethod) each).setDownstreamDependencies(downstream);
        Set<ITestNGMethod> upstream = new HashSet<>(graph.getUpstreamDependenciesFor(each));
        ((BaseTestMethod) each).setUpstreamDependencies(upstream);
      }
    }
  }

  /**
   * Gives each method the dependencies that it declares, found among {@code methods}.
   *
   * <p>This method does not build the graph that the run uses, on purpose. That graph would also
   * take in the methods that an interceptor is about to remove. It would check their {@code
   * dependsOnGroups}, fail on a cycle that the removal breaks, and create their lazy instances. An
   * interceptor that removes a method prevents all of these.
   *
   * <p>A dependency that this method cannot find is left out, and is not an error. The graph of the
   * run reports it, because that graph holds only the methods that run.
   *
   * <p>So the result holds only the {@code dependsOnMethods} and {@code dependsOnGroups}
   * dependencies. It does not hold the order that {@code preserve-order} or {@code
   * group-by-instances} adds. The methods get that fuller set from the graph, when it exists.
   */
  private void publishDeclaredDependencies(ITestNGMethod[] methods) {
    DependencyMap dependencyMap = new DependencyMap(methods);
    Map<ITestNGMethod, Set<ITestNGMethod>> upstream = new IdentityHashMap<>();
    Map<ITestNGMethod, Set<ITestNGMethod>> downstream = new IdentityHashMap<>();

    for (ITestNGMethod each : methods) {
      for (String name : each.getMethodsDependedUpon()) {
        resolve(() -> Collections.singletonList(dependencyMap.getMethodDependingOn(name, each)))
            .forEach(upon -> bind(upstream, downstream, each, upon));
      }
      for (String group : each.getGroupsDependedUpon()) {
        resolve(() -> dependencyMap.getMethodsThatBelongTo(group, each))
            .forEach(upon -> bind(upstream, downstream, each, upon));
      }
    }

    for (ITestNGMethod each : methods) {
      if (each instanceof BaseTestMethod) {
        ((BaseTestMethod) each)
            .setUpstreamDependencies(upstream.getOrDefault(each, Collections.emptySet()));
        ((BaseTestMethod) each)
            .setDownstreamDependencies(downstream.getOrDefault(each, Collections.emptySet()));
      }
    }
  }

  /**
   * Returns the methods that {@code resolution} finds, or an empty list when it throws. The graph
   * of the run reports a dependency that cannot be found.
   */
  private static List<ITestNGMethod> resolve(Supplier<List<ITestNGMethod>> resolution) {
    try {
      return resolution.get();
    } catch (RuntimeException failedToResolve) {
      return Collections.emptyList();
    }
  }

  /**
   * Records in both maps that {@code method} must run after {@code upon}.
   *
   * <p>This method skips {@code upon} when it is {@code null}, or when it is {@code method} itself.
   * The check compares by identity, so it drops only the edge from a method object to the same
   * object.
   */
  @SuppressWarnings("ReferenceEquality")
  private static void bind(
      Map<ITestNGMethod, Set<ITestNGMethod>> upstream,
      Map<ITestNGMethod, Set<ITestNGMethod>> downstream,
      ITestNGMethod method,
      ITestNGMethod upon) {
    if (upon == null || upon == method) {
      return;
    }
    upstream.computeIfAbsent(method, k -> new HashSet<>()).add(upon);
    downstream.computeIfAbsent(upon, k -> new HashSet<>()).add(method);
  }

  /**
   * Tells if the user added a method interceptor.
   *
   * <p>{@code init} empties the list of interceptors. Then it adds one built-in interceptor, {@link
   * PreserveOrderMethodInterceptor} or {@link InstanceOrderingMethodInterceptor}, before any
   * listener can add one. So a second interceptor in the list comes from the user.
   */
  private boolean hasUserMethodInterceptors() {
    return m_methodInterceptors.size() > 1;
  }

  /** Runs the method interceptors on {@code methods}, and returns the methods to run, in order. */
  private ITestNGMethod[] intercept(ITestNGMethod[] methods) {

    // An interceptor gets every test method of this <test>, also the methods that take part in a
    // dependency. So give each method its declared dependencies before the interceptors decide
    // what to remove and what to move. Do this only with a user interceptor. The built-in
    // interceptors do not read the dependencies.
    if (hasUserMethodInterceptors()) {
      publishDeclaredDependencies(methods);
    }

    List<IMethodInstance> methodInstances =
        MethodHelper.methodsToMethodInstances(Arrays.asList(methods));

    List<IMethodInterceptor> original =
        sort(m_methodInterceptors, m_configuration.getListenerComparator());
    for (IMethodInterceptor m_methodInterceptor : original) {
      methodInstances = m_methodInterceptor.intercept(methodInstances, this);
    }

    List<ITestNGMethod> result = MethodHelper.methodInstancesToMethods(methodInstances);

    // Build the ClassMethodMap again from the methods that the interceptors returned. The old map
    // can hold a method that an interceptor removed. Then the @AfterClass methods of its class
    // would never run.
    this.m_classMethodMap = new ClassMethodMap(result, null);

    ITestNGMethod[] resultArray = result.toArray(new ITestNGMethod[0]);
    interceptedTestMethods = resultArray;
    hasInterceptedMethods = true;

    // Build the ConfigurationGroupMethods again from the new list, when the interceptors changed
    // the number of test methods.
    if (resultArray.length != testMethodsContainer.getItems().length) {
      ConfigurationGroupMethods current = requireGroupMethods();
      m_groupMethods =
          new ConfigurationGroupMethods(
              new TestMethodContainer(() -> resultArray),
              current.getBeforeGroupsMethods(),
              current.getAfterGroupsMethods());
    }

    // With a user interceptor, the order that the interceptors return is the order of the run.
    // Record that order as the intercepted priority of each method.
    if (hasUserMethodInterceptors()) {
      for (int i = 0; i < resultArray.length; ++i) {
        resultArray[i].setInterceptedPriority(i);
      }
    }

    return resultArray;
  }

  /**
   * Creates the workers that run {@code methods}.
   *
   * <p>Each test method gets a worker of its own, except in these cases:
   *
   * <ul>
   *   <li>The class of the method has {@code @Test(singleThreaded = true)}.
   *   <li>The {@code parallel} attribute is {@code "classes"}.
   * </ul>
   *
   * <p>In both cases, one worker runs all the methods of that class, so that they run in one
   * thread. With {@code parallel="instances"} and {@code group-by-instances}, one worker runs all
   * the methods of each test instance.
   */
  @Override
  public List<IWorker<ITestNGMethod>> createWorkers(List<ITestNGMethod> methods) {
    AbstractParallelWorker.Arguments args =
        new AbstractParallelWorker.Arguments.Builder()
            .classMethodMap(requireClassMethodMap())
            .configMethods(requireGroupMethods())
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

  private void afterRun() {
    // Run the @AfterTest methods
    ITestNGMethod[] testConfigurationMethods = getAfterTestConfigurationMethods();
    invokeTestConfigurations(testConfigurationMethods);

    //
    // Record the end time
    //
    m_endInstant = Instant.now();

    dumpInvokedMethods();

    // Call onFinish on the test listeners
    fireEvent(false /*stop*/);
    removeAttribute(IObjectDispenser.GUICE_HELPER);
  }

  /**
   * Logs the start of the run, with the number of classes and the groups. Then logs each test class
   * with its methods. Both happen only at a verbose level of 3 or more.
   */
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
   * Calls {@code onStart} or {@code onFinish} on each test listener, and then on the exit code
   * listener.
   *
   * <p>{@code onFinish} goes to the test listeners in the reverse order. After {@code onFinish},
   * this method also clears the cached names of the methods of this {@code <test>}.
   *
   * @param isStart {@code true} to call {@code onStart}, {@code false} to call {@code onFinish}.
   */
  private void fireEvent(boolean isStart) {
    if (isStart) {
      for (ITestListener itl :
          ListenerOrderDeterminer.order(m_testListeners, m_configuration.getListenerComparator())) {
        itl.onStart(this);
      }
      getExitCodeListener().onStart(this);

    } else {
      List<ITestListener> testListenersReversed =
          ListenerOrderDeterminer.reversedOrder(
              m_testListeners, m_configuration.getListenerComparator());
      for (ITestListener itl : testListenersReversed) {
        itl.onFinish(this);
      }
      getExitCodeListener().onFinish(this);
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

  /** Returns the time when this {@code <test>} started, as a {@link Date}. */
  @Deprecated
  @Override
  public Date getStartDate() {
    return Date.from(m_startInstant);
  }

  /**
   * Returns the time when this {@code <test>} ended, as a {@link Date}, or {@code null} while it
   * still runs.
   */
  @Deprecated
  @Override
  public @Nullable Date getEndDate() {
    return m_endInstant == null ? null : Date.from(m_endInstant);
  }

  @Override
  public Instant getStartInstant() {
    return m_startInstant;
  }

  @Override
  public @Nullable Instant getEndInstant() {
    return m_endInstant;
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

  /** Returns the suite of this {@code <test>}. */
  @Override
  public ISuite getSuite() {
    return m_suite;
  }

  @Override
  public ITestNGMethod[] getAllTestMethods() {
    return testMethodsContainer.getItems();
  }

  @Override
  public @Nullable String getHost() {
    return m_host;
  }

  @Override
  public Collection<ITestNGMethod> getExcludedMethods() {
    Map<ITestNGMethod, ITestNGMethod> vResult = new HashMap<>();

    for (ITestNGMethod m : m_excludedMethods) {
      vResult.put(m, m);
    }

    return vResult.keySet();
  }

  /** Returns the results of the configuration methods that failed. */
  @Override
  public IResultMap getFailedConfigurations() {
    return m_failedConfigurations;
  }

  @Override
  public IResultMap getConfigurationsScheduledForInvocation() {
    return m_configsToBeInvoked;
  }

  /** Returns the results of the configuration methods that passed. */
  @Override
  public IResultMap getPassedConfigurations() {
    return m_passedConfigurations;
  }

  /** Returns the results of the configuration methods that TestNG skipped. */
  @Override
  public IResultMap getSkippedConfigurations() {
    return m_skippedConfigurations;
  }

  /**
   * Adds a time-out failure for each test invocation that has no result yet, and freezes the
   * results.
   *
   * <p>{@link SuiteRunner} calls this method when the time-out of the suite stops a {@code <test>}.
   * After this call, the runner ignores new results, for example from a worker that the time-out
   * cancelled. A result whose listeners still run goes into the map of its status now, so that the
   * reports can see it.
   *
   * @param timeOut the time-out of the suite, in milliseconds.
   * @return the time-out failures that this method added.
   */
  List<ITestResult> addTimeoutFailuresForUnfinishedInvocations(long timeOut) {
    List<ITestResult> created = new ArrayList<>();
    synchronized (resultLock) {
      ITestNGMethod[] methods = methodsEligibleForTimeoutReporting();
      for (ITestNGMethod method : methods) {
        int missing = unfinishedInvocationCount(method);
        for (int i = 0; i < missing; i++) {
          ITestResult failure = createTimeoutFailure(method, timeOut);
          m_failedTests.addResult(failure);
          created.add(failure);
        }
      }
      if (m_endInstant == null) {
        m_endInstant = Instant.now();
      }
      resultsFrozen = true;
      publishAdmittedResults();
    }
    return created;
  }

  /**
   * Tells if the results are frozen. The runner ignores new results after {@link
   * #addTimeoutFailuresForUnfinishedInvocations} freezes them.
   *
   * @return {@code true} when the results are frozen.
   */
  public boolean resultsFrozen() {
    return resultsFrozen;
  }

  /**
   * Records that a test invocation has started and has no result yet. {@link
   * #addTimeoutFailuresForUnfinishedInvocations} reads this count. After the results freeze, this
   * method does nothing.
   *
   * @param method the test method of the invocation.
   */
  public void markInvocationStarted(ITestNGMethod method) {
    synchronized (resultLock) {
      if (resultsFrozen) {
        return;
      }
      Integer n = inFlightInvocations.get(method);
      inFlightInvocations.put(method, n == null ? 1 : n + 1);
    }
  }

  /**
   * Records that a test invocation has its result. This method also works after a freeze. A test
   * whose {@code @AfterMethod} still runs is not an unfinished invocation.
   *
   * @param method the test method of the invocation.
   */
  public void markInvocationFinished(ITestNGMethod method) {
    synchronized (resultLock) {
      Integer n = inFlightInvocations.get(method);
      if (n == null) {
        return;
      }
      if (n <= 1) {
        inFlightInvocations.remove(method);
      } else {
        inFlightInvocations.put(method, n - 1);
      }
    }
  }

  /**
   * Calls the test listeners for {@code tr}, unless the results are frozen.
   *
   * <p>This method checks for a freeze while it holds {@code resultLock}. In the same step, it
   * removes a finished invocation from the count of running invocations. So a freeze cannot add a
   * second result for the same invocation.
   *
   * <p>The listeners run after this method releases the lock, so a slow listener cannot delay a
   * freeze. This method puts the result into a result map only after the listeners finish, because
   * a listener can change the status.
   *
   * @param tr the result to report.
   * @param listeners the test listeners to call first.
   * @param extraListeners the test listeners to call after {@code listeners}.
   * @return {@code false} when the results are frozen and no listener ran.
   */
  public boolean notifyTestListenersIfNotFrozen(
      ITestResult tr, List<ITestListener> listeners, List<ITestListener> extraListeners) {
    boolean finished = tr.getStatus() != ITestResult.STARTED;
    synchronized (resultLock) {
      if (resultsFrozen) {
        return false;
      }
      if (finished) {
        admitFinishedResult(tr);
      }
    }
    TestListenerHelper.runTestListeners(tr, listeners);
    TestListenerHelper.runTestListeners(tr, extraListeners);
    if (finished) {
      classifyAdmittedResult(tr);
    }
    return true;
  }

  /**
   * Marks a finished invocation as admitted. The caller holds {@code resultLock}. A later time-out
   * then sees the invocation, and does not add a time-out failure for it.
   */
  private void admitFinishedResult(ITestResult tr) {
    ITestNGMethod method = tr.getMethod();
    Integer n = inFlightInvocations.get(method);
    if (n != null) {
      if (n <= 1) {
        inFlightInvocations.remove(method);
      } else {
        inFlightInvocations.put(method, n - 1);
      }
    }
    admittedResults.add(tr);
  }

  /**
   * Puts each admitted result into the map of its current status. The reports can then see it while
   * a listener still runs. {@link #classifyAdmittedResult} runs after that listener, and can move
   * the result when the status changed.
   */
  private void publishAdmittedResults() {
    for (ITestResult result : admittedResults) {
      placeClassifiedResult(result);
    }
  }

  /**
   * Puts an admitted result into the map of its status, after the listeners ran. This works after a
   * freeze too, because the result was admitted before it.
   */
  private void classifyAdmittedResult(ITestResult tr) {
    synchronized (resultLock) {
      if (!admittedResults.remove(tr)) {
        return;
      }
      placeClassifiedResult(tr);
    }
  }

  /**
   * Puts {@code tr} into the map of its status, when no map holds it yet. This method does not
   * change the count of running invocations. After the results freeze, it does nothing.
   *
   * <p>When {@code tr} still has the status {@code STARTED} or {@code CREATED}, this method changes
   * the status to {@code FAILURE}.
   *
   * @param tr the result to record.
   */
  public void classifyIfNeeded(ITestResult tr) {
    synchronized (resultLock) {
      if (resultsFrozen) {
        return;
      }
      if (recordedContains(tr)) {
        return;
      }
      if (admittedResults.remove(tr)) {
        placeClassifiedResult(tr);
        return;
      }
      int status = tr.getStatus();
      if (status == ITestResult.STARTED || status == ITestResult.CREATED) {
        // An IHookable did not call the test method, and testng.ignore.callback.skip is set.
        tr.setStatus(ITestResult.FAILURE);
      }
      placeClassifiedResult(tr);
    }
  }

  private boolean recordedContains(ITestResult tr) {
    return m_passedTests.getAllResults().contains(tr)
        || m_failedTests.getAllResults().contains(tr)
        || m_skippedTests.getAllResults().contains(tr)
        || m_failedButWithinSuccessPercentageTests.getAllResults().contains(tr);
  }

  private void placeClassifiedResult(ITestResult tr) {
    m_passedTests.removeResult(tr);
    m_failedTests.removeResult(tr);
    m_skippedTests.removeResult(tr);
    m_failedButWithinSuccessPercentageTests.removeResult(tr);
    switch (tr.getStatus()) {
      case ITestResult.SUCCESS:
        m_passedTests.addResult(tr);
        break;
      case ITestResult.SKIP:
        m_skippedTests.addResult(tr);
        break;
      case ITestResult.FAILURE:
        m_failedTests.addResult(tr);
        break;
      case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
        m_failedButWithinSuccessPercentageTests.addResult(tr);
        break;
      default:
        break;
    }
  }

  private ITestNGMethod[] methodsEligibleForTimeoutReporting() {
    if (hasInterceptedMethods) {
      return interceptedTestMethods;
    }
    return getAllTestMethods();
  }

  private static int expectedInvocationCount(ITestNGMethod method) {
    int perRow = Math.max(1, method.getParameterInvocationCount());
    return Math.max(1, method.getInvocationCount() * perRow);
  }

  private int unfinishedInvocationCount(ITestNGMethod method) {
    int recorded = recordedResultCount(method) + pendingAdmittedCount(method);
    int missing = expectedInvocationCount(method) - recorded;
    if (missing <= 0 && method.hasMoreInvocation()) {
      missing = 1;
    }
    missing = Math.max(0, missing);
    Integer inFlight = inFlightInvocations.get(method);
    int running = inFlight == null ? 0 : inFlight;
    return Math.max(missing, running);
  }

  /** Creates a time-out failure for {@code method}. The caller adds it to {@code m_failedTests}. */
  private ITestResult createTimeoutFailure(ITestNGMethod method, long timeOut) {
    ThreadTimeoutException exception = new ThreadTimeoutException(method, timeOut);
    ITestResult result = TestResult.newTestResultWithCauseAs(method, this, exception);
    result.setStatus(ITestResult.FAILURE);
    return result;
  }

  private int recordedResultCount(ITestNGMethod method) {
    return getPassedTests(method).size()
        + getFailedTests(method).size()
        + getSkippedTests(method).size()
        + m_failedButWithinSuccessPercentageTests.getResults(method).size();
  }

  // Compare by identity on purpose. The count of running invocations keys each method by its
  // object, not by equals().
  @SuppressWarnings("ReferenceEquality")
  private int pendingAdmittedCount(ITestNGMethod method) {
    int pending = 0;
    for (ITestResult result : admittedResults) {
      if (result.getMethod() == method) {
        pending++;
      }
    }
    return pending;
  }

  @Override
  public void addPassedTest(ITestNGMethod tm, ITestResult tr) {
    synchronized (resultLock) {
      if (resultsFrozen) {
        return;
      }
      m_passedTests.addResult(tr);
    }
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
    synchronized (resultLock) {
      if (resultsFrozen) {
        return;
      }
      m_skippedTests.addResult(tr);
    }
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
    return m_configurationListeners.stream()
        .map(ClassBasedWrapper::wrap)
        .distinct()
        .map(ClassBasedWrapper::unWrap)
        .collect(Collectors.toUnmodifiableList());
  }

  private void logFailedTest(ITestResult tr, boolean withinSuccessPercentage) {
    synchronized (resultLock) {
      if (resultsFrozen) {
        return;
      }
      if (withinSuccessPercentage) {
        m_failedButWithinSuccessPercentageTests.addResult(tr);
      } else {
        m_failedTests.addResult(tr);
      }
    }
  }

  private static void log(String s) {
    Utils.log("TestRunner", 3, s);
  }

  /**
   * Returns the verbose level of TestNG. The level is global, so it applies to the whole JVM.
   *
   * @return the verbose level.
   */
  public static int getVerbose() {
    return Utils.getVerbose();
  }

  /**
   * Sets the verbose level of TestNG. The level is global, so it applies to every runner in the
   * JVM, not only to this one.
   *
   * @param n the new verbose level.
   */
  // TODO: this instance method changes a static setting. Should it go?
  public void setVerbose(int n) {
    Utils.setVerbose(n);
  }

  // TODO: remove this method, and use addListener() instead. Find out first what that changes.
  void addTestListener(ITestListener listener) {
    insertTestListener(listener, m_testListeners.size());
  }

  /**
   * Adds one of the listeners of TestNG itself, before the other test listeners.
   *
   * <p>The other listeners read what this listener records. TestNG calls {@code onTestStart} in the
   * order of the list, and {@link #addTestListener} adds a listener at the end. At the end, this
   * listener would hear of a test start only after the reporters looked for what it records.
   *
   * <p>The position is the only difference from {@link #addTestListener}. A {@link
   * ListenerComparator} sorts the whole list later, and can still move this listener.
   *
   * @param listener an internal listener that the other listeners depend on.
   */
  void addInternalTestListener(ITestListener listener) {
    insertTestListener(listener, 0);
  }

  private void insertTestListener(ITestListener listener, int index) {
    boolean found =
        m_testListeners.stream()
            .anyMatch(iTestListener -> iTestListener.getClass().equals(listener.getClass()));
    if (!found) {
      m_testListeners.add(index, listener);
    }
  }

  /**
   * Adds a listener to this {@code <test>}, and to its suite.
   *
   * <p>The listener goes into each list of listeners whose interface it implements, for example
   * {@link ITestListener} or {@link IMethodInterceptor}. A few interfaces work in a different way:
   *
   * <ul>
   *   <li>An {@link IHookable} or an {@link IConfigurable} replaces the one that the configuration
   *       had. The configuration holds one of each.
   *   <li>A new {@link IExecutionListener} gets its {@code onExecutionStart} call at once, because
   *       the run has already started. TestNG keeps one execution listener of each class.
   * </ul>
   *
   * @param listener the listener to add.
   */
  public void addListener(ITestNGListener listener) {
    if (listener instanceof IMethodInterceptor) {
      m_methodInterceptors.add((IMethodInterceptor) listener);
    }
    if (listener instanceof ITestListener) {
      // The invoker reads this list through getTestListeners() at each event. So a listener that
      // is added now still gets the events.
      addTestListener((ITestListener) listener);
    }
    if (listener instanceof IClassListener) {
      IClassListener classListener = (IClassListener) listener;
      m_classListeners.putIfAbsent(classListener.getClass(), classListener);
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
    if (listener instanceof IParameterResolver) {
      parameterResolverHolder.addResolver((IParameterResolver) listener);
    }

    if (listener instanceof IExecutionVisualiser) {
      IExecutionVisualiser l = (IExecutionVisualiser) listener;
      visualisers.add(l);
    }
    m_suite.addListener(listener);
  }

  void addConfigurationListener(IConfigurationListener icl) {
    insertConfigurationListener(icl, m_configurationListeners.size());
  }

  /**
   * Adds one of the configuration listeners of TestNG itself, before the other configuration
   * listeners.
   *
   * <p>This method does for configuration listeners what {@link #addInternalTestListener} does for
   * test listeners. TestNG calls {@code beforeConfiguration} in the order of the list too. So a
   * listener that the others read must come first.
   *
   * <p>{@code TestListenerHelper#runPostConfigurationListeners} reverses the order. So the first
   * listener here is the last to hear that a configuration method finished. That is the right
   * place. An internal listener can drop what it knows about a finished configuration method. It
   * then drops it only after the reporters read it.
   *
   * <p>Do not depend on this order, because other code can change it:
   *
   * <ul>
   *   <li>A {@code ListenerComparator} sorts this list before the reversal, and can move any
   *       listener.
   *   <li>TestNG puts the preferential listeners after the regular ones.
   *   <li>The invoker adds {@link ConfigurationListener} after the reversal. So the result goes
   *       into {@code m_passedConfigurations}, where the reports read it, only after every listener
   *       here hears of it.
   * </ul>
   *
   * @param icl an internal listener that the other listeners depend on.
   */
  void addInternalConfigurationListener(IConfigurationListener icl) {
    insertConfigurationListener(icl, 0);
  }

  private void insertConfigurationListener(IConfigurationListener icl, int index) {
    boolean alreadyAdded =
        m_configurationListeners.stream().anyMatch(each -> each.getClass().equals(icl.getClass()));
    if (!alreadyAdded) {
      m_configurationListeners.add(index, icl);
    }
  }

  private void setExitCodeListener(ITestListener exitCodeListener) {
    this.exitCodeListener = exitCodeListener;
  }

  @Override
  public ITestListener getExitCodeListener() {
    return Objects.requireNonNull(exitCodeListener, "ExitCodeListener cannot be null.");
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
      // Remove each scheduled result of the same method. ResultMap.removeResult(ITestResult) would
      // remove only a result that equals this result object.
      m_configsToBeInvoked
          .getAllResults()
          .removeIf(tr -> Objects.equals(tr.getMethod(), itr.getMethod()));
    }
  }

  void addMethodInterceptor(IMethodInterceptor methodInterceptor) {
    // Do not add the same interceptor twice. A listener can implement both ITestListener and
    // IMethodInterceptor, so it can reach this runner by more than one path.
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
  public @Nullable Object getAttribute(String name) {
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
  public @Nullable Object removeAttribute(String name) {
    return m_attributes.removeAttribute(name);
  }

  @Override
  public IInjectorFactory getInjectorFactory() {
    return this.m_injectorFactory;
  }
}
