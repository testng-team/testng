package org.testng;

import static org.testng.ListenerComparator.sort;
import static org.testng.internal.Utils.defaultIfStringEmpty;
import static org.testng.internal.Utils.isStringEmpty;
import static org.testng.internal.Utils.isStringNotEmpty;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import org.jspecify.annotations.Nullable;
import org.testng.SuiteRunner.TestListenersContainer;
import org.testng.annotations.ITestAnnotation;
import org.testng.internal.ClassHelper;
import org.testng.internal.Configuration;
import org.testng.internal.DynamicGraph;
import org.testng.internal.ExitCode;
import org.testng.internal.IConfiguration;
import org.testng.internal.ListenerOrderDeterminer;
import org.testng.internal.MethodSorting;
import org.testng.internal.ObjectBag;
import org.testng.internal.OverrideProcessor;
import org.testng.internal.ReporterConfig;
import org.testng.internal.RuntimeBehavior;
import org.testng.internal.Utils;
import org.testng.internal.Version;
import org.testng.internal.annotations.DefaultAnnotationTransformer;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.JDK15AnnotationFinder;
import org.testng.internal.invokers.SuiteRunnerMap;
import org.testng.internal.invokers.objects.GuiceContext;
import org.testng.internal.objects.DefaultTestObjectFactory;
import org.testng.internal.objects.Dispenser;
import org.testng.internal.objects.IObjectDispenser;
import org.testng.internal.objects.pojo.BasicAttributes;
import org.testng.internal.objects.pojo.CreationAttributes;
import org.testng.internal.reporters.ParameterSnapshotReader;
import org.testng.internal.reporters.ParameterSnapshots;
import org.testng.internal.thread.graph.SuiteWorkerFactory;
import org.testng.log4testng.Logger;
import org.testng.reporters.EmailableReporter2;
import org.testng.reporters.FailedReporter;
import org.testng.reporters.JUnitReportReporter;
import org.testng.reporters.PerSuiteXMLReporter;
import org.testng.reporters.VerboseReporter;
import org.testng.reporters.XMLReporter;
import org.testng.reporters.jq.Main;
import org.testng.thread.IThreadWorkerFactory;
import org.testng.util.Strings;
import org.testng.xml.IPostProcessor;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.xml.internal.Parser;
import org.testng.xml.internal.TestNamesMatcher;
import org.testng.xml.internal.XmlSuiteUtils;

/**
 * The main entry point for running TestNG.
 *
 * <p>You can create a {@code TestNG} object and run tests in several ways:
 *
 * <ul>
 *   <li>From suite files, with {@link #setTestSuites(List)}.
 *   <li>From suites that you build in Java, with {@link #setXmlSuites(List)}.
 *   <li>From test classes, with {@link #setTestClasses(Class[])}.
 * </ul>
 *
 * <p>You can also choose the groups to include or exclude, set parameters, and add listeners. Then
 * call {@link #run()}, and read the result with {@link #getStatus()}.
 *
 * <p>The {@code testng-cli} module defines the command line options. See the TestNG documentation
 * for more details.
 *
 * @author <a href = "mailto:cedric&#64;beust.com">Cedric Beust</a>
 */
// FIXME: should support more than simple paths for suite xmls
@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class TestNG {

  /** The logger of this class. */
  private static final Logger LOGGER = Logger.getLogger(TestNG.class);

  /** The default name of a suite that TestNG builds from command line options. */
  public static final String DEFAULT_COMMAND_LINE_SUITE_NAME = "Command line suite";

  /** The default name of a test that TestNG builds from command line options. */
  public static final String DEFAULT_COMMAND_LINE_TEST_NAME = "Command line test";

  private static final String DEFAULT_THREADPOOL_FACTORY =
      "org.testng.internal.thread.DefaultThreadPoolExecutorFactory";

  /** The default directory for the reports. Keep it public, because the Eclipse plugin uses it. */
  public static final String DEFAULT_OUTPUTDIR = "test-output";

  /** The suite file that TestNG looks for in a jar when no path is given. */
  private static final String DEFAULT_XML_PATH_IN_JAR = "testng.xml";

  /**
   * The number of suites that run at the same time when no number is given. One means that the
   * suites run one after another.
   */
  private static final Integer DEFAULT_SUITE_THREAD_POOL_SIZE = 1;

  private static @Nullable TestNG m_instance;

  private @Nullable List<String> m_commandLineMethods;
  /** The suites to run. */
  protected List<XmlSuite> m_suites = new ArrayList<>();

  private @Nullable List<XmlSuite> m_cmdlineSuites;
  private String m_outputDir = DEFAULT_OUTPUTDIR;
  private String @Nullable [] m_includedGroups;
  private String @Nullable [] m_excludedGroups;
  /** Whether TestNG adds its default listeners, such as the default reporters. */
  protected boolean m_useDefaultListeners = true;

  private boolean m_failIfAllTestsSkipped = false;
  private final List<String> m_listenersToSkipFromBeingWiredIn = new ArrayList<>();

  private @Nullable ITestRunnerFactory m_testRunnerFactory;

  // The listeners of the run, one for each class. TestNG ignores a second listener of the same
  // class, and logs a warning.
  private final Map<Class<? extends IClassListener>, IClassListener> m_classListeners =
      new LinkedHashMap<>();
  private final Map<Class<? extends ITestListener>, ITestListener> m_testListeners =
      new LinkedHashMap<>();
  private final Map<Class<? extends ISuiteListener>, ISuiteListener> m_suiteListeners =
      new LinkedHashMap<>();
  private final Map<Class<? extends IReporter>, IReporter> m_reporters = new LinkedHashMap<>();
  private final Map<Class<? extends IDataProviderListener>, IDataProviderListener>
      m_dataProviderListeners = new LinkedHashMap<>();
  private final Map<Class<? extends IDataProviderInterceptor>, IDataProviderInterceptor>
      m_dataProviderInterceptors = new LinkedHashMap<>();
  private final Map<Class<? extends IParameterResolver>, IParameterResolver> m_parameterResolvers =
      new LinkedHashMap<>();

  /**
   * A verbose level of 1. This is the default level when the {@code testng.default.verbose} system
   * property is not set.
   */
  public static final Integer DEFAULT_VERBOSE = 1;

  // Settings for the suites that TestNG builds from command line options
  private int m_threadCount = -1;
  private XmlSuite.@Nullable ParallelMode m_parallelMode = null;
  private XmlSuite.@Nullable FailurePolicy m_configFailurePolicy;
  private Class<?> @Nullable [] m_commandLineTestClasses;

  private String m_defaultSuiteName = DEFAULT_COMMAND_LINE_SUITE_NAME;
  private String m_defaultTestName = DEFAULT_COMMAND_LINE_TEST_NAME;

  private final Map<String, Integer> m_methodDescriptors = new HashMap<>();

  private final Set<XmlMethodSelector> m_selectors = new LinkedHashSet<>();

  private static final ITestObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultTestObjectFactory();
  private ITestObjectFactory m_objectFactory = DEFAULT_OBJECT_FACTORY;

  private final Map<Class<? extends IInvokedMethodListener>, IInvokedMethodListener>
      m_invokedMethodListeners = new LinkedHashMap<>();

  private @Nullable Integer m_dataProviderThreadCount = null;

  private @Nullable String m_jarPath;
  /** The path of the suite file inside the jar. */
  private String m_xmlPathInJar = DEFAULT_XML_PATH_IN_JAR;

  private List<String> m_stringSuites = new ArrayList<>();
  private final List<Class<? extends ITestNGListener>> m_listenerClasses = new ArrayList<>();

  private @Nullable IHookable m_hookable;
  private @Nullable IConfigurable m_configurable;

  /** The time when the run ended, in milliseconds. */
  protected long m_end;

  /** The time when the run started, in milliseconds. */
  protected long m_start;

  private final Map<Class<? extends IAlterSuiteListener>, IAlterSuiteListener>
      m_alterSuiteListeners = new LinkedHashMap<>();

  private boolean m_isInitialized = false;
  private boolean isSuiteInitialized = false;
  private final org.testng.internal.ExitCodeListener exitCodeListener =
      new org.testng.internal.ExitCodeListener();
  private @Nullable ExitCode exitCode;
  private final Map<Class<? extends IExecutionVisualiser>, IExecutionVisualiser>
      m_executionVisualisers = new LinkedHashMap<>();

  /** Creates a TestNG object that uses the default listeners and reporters. */
  public TestNG() {
    init(true);
    if (RuntimeBehavior.isMemoryFriendlyMode()) {
      Logger.getLogger(TestNG.class).warn("TestNG is running in memory friendly mode.");
    }
  }

  /**
   * Creates a TestNG object, with or without the default listeners and reporters.
   *
   * <p>Maven 2 passes {@code false}, so that TestNG writes no output of its own.
   *
   * @param useDefaultListeners whether TestNG adds its default listeners and reporters.
   */
  public TestNG(boolean useDefaultListeners) {
    init(useDefaultListeners);
  }

  private void init(boolean useDefaultListeners) {
    m_instance = this;

    m_useDefaultListeners = useDefaultListeners;
    m_configuration = new Configuration();
  }

  /**
   * Sets whether the run fails when TestNG skips every test and runs nothing.
   *
   * <p>This happens, for example, when every test throws a {@link SkipException}. With this setting
   * on, {@link #run()} then throws a {@link TestNGException}. The command line catches it, and
   * reports the run as failed.
   *
   * @param failIfAllTestsSkipped whether the run fails when TestNG skips every test.
   */
  public void toggleFailureIfAllTestsWereSkipped(boolean failIfAllTestsSkipped) {
    this.m_failIfAllTestsSkipped = failIfAllTestsSkipped;
  }

  /**
   * Names the listeners that TestNG must not load through {@link ServiceLoader}.
   *
   * @param listeners the full class names of the listeners.
   */
  public void setListenersToSkipFromBeingWiredInViaServiceLoaders(String... listeners) {
    m_listenersToSkipFromBeingWiredIn.addAll(Arrays.asList(listeners));
  }

  /**
   * Returns the exit status of the run.
   *
   * <p>The status is 8 when the run has no test results, not even skipped ones. This method checks
   * that first, so 8 hides every other value. It hides even a failure that {@link
   * #reportRunFailure(TestNGException)} recorded (GITHUB-3566).
   *
   * <p>Otherwise, the status is the sum of these values:
   *
   * <ul>
   *   <li>1 when a test or a configuration method failed, or when {@link
   *       #reportRunFailure(TestNGException)} recorded a failure.
   *   <li>2 when a test or a configuration method was skipped.
   *   <li>4 when a test failed within its success percentage.
   * </ul>
   *
   * <p>The status is 0 when none of these happened.
   *
   * @return the exit status.
   * @throws NullPointerException when the run has test results, but {@link #run()} threw before it
   *     finished.
   */
  public int getStatus() {
    if (exitCodeListener.noTestsFound()) {
      return ExitCode.HAS_NO_TEST;
    }
    return requireExitCode().getExitCode();
  }

  /**
   * Sets the directory where TestNG writes the reports. An empty value changes nothing.
   *
   * @param outputdir the directory.
   */
  public void setOutputDirectory(final String outputdir) {
    if (isStringNotEmpty(outputdir)) {
      m_outputDir = outputdir;
    }
  }

  /**
   * Sets whether TestNG adds its default listeners. Call it before {@link #run()}.
   *
   * <p>The default listeners include reporters such as these:
   *
   * <ul>
   *   <li>{@link org.testng.reporters.TestHTMLReporter}
   *   <li>{@link org.testng.reporters.JUnitXMLReporter}
   *   <li>{@link org.testng.reporters.XMLReporter}
   * </ul>
   *
   * @param useDefaultListeners whether to add the default listeners.
   */
  public void setUseDefaultListeners(boolean useDefaultListeners) {
    m_useDefaultListeners = useDefaultListeners;
  }

  /**
   * Sets the comparator that orders the listeners.
   *
   * @param listenerComparator the comparator.
   */
  public void setListenerComparator(ListenerComparator listenerComparator) {
    this.m_configuration.setListenerComparator(listenerComparator);
  }

  /**
   * Sets the comparator that orders the listeners, by class.
   *
   * <p>TestNG creates the comparator with the object factory that it uses at the time of the call.
   *
   * @param listenerComparatorClass the class of the comparator.
   */
  public void setListenerComparatorClass(
      Class<? extends ListenerComparator> listenerComparatorClass) {
    setListenerComparator(m_objectFactory.newInstance(listenerComparatorClass));
  }

  /**
   * Returns the comparator that orders the listeners.
   *
   * @return the comparator, or {@code null} when there is none.
   */
  public @Nullable ListenerComparator getListenerComparator() {
    return m_configuration.getListenerComparator();
  }

  /**
   * Sets the jar that holds the tests.
   *
   * <p>TestNG runs the suite file in the jar. When the jar has no suite file, TestNG runs the
   * classes in the jar.
   *
   * @param jarPath the path of the jar, or {@code null}.
   */
  public void setTestJar(@Nullable String jarPath) {
    m_jarPath = jarPath;
  }

  /**
   * Sets the path of the suite file inside the test jar.
   *
   * @param xmlPathInJar the path.
   */
  public void setXmlPathInJar(String xmlPathInJar) {
    m_xmlPathInJar = xmlPathInJar;
  }

  private void parseSuiteFiles() {
    IPostProcessor processor = getProcessor();
    for (XmlSuite s : m_suites) {
      if (s.isParsed()) {
        continue;
      }
      for (String suiteFile : s.getSuiteFiles()) {
        try {
          String fileNameToUse = s.getFileName();
          if (fileNameToUse == null || fileNameToUse.trim().isEmpty()) {
            fileNameToUse = suiteFile;
          }
          Collection<XmlSuite> childSuites = Parser.parse(fileNameToUse, processor);
          for (XmlSuite cSuite : childSuites) {
            cSuite.setParentSuite(s);
            s.getChildSuites().add(cSuite);
          }
        } catch (IOException e) {
          e.printStackTrace(System.out);
        }
      }
    }
  }

  private OverrideProcessor getProcessor() {
    return new OverrideProcessor(m_includedGroups, m_excludedGroups);
  }

  private Collection<XmlSuite> parseSuite(String suitePath) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("suiteXmlPath: \"" + suitePath + "\"");
    }
    try {
      return Parser.parse(suitePath, getProcessor());
    } catch (IOException e) {
      throw new TestNGException("Failed to parse suite: " + suitePath, e);
    } catch (Exception ex) {
      // The parser can wrap the real error, for example a YAML error. Find the deepest cause.
      Throwable t = ex;
      while (t.getCause() != null) {
        t = t.getCause();
      }
      if (t instanceof TestNGException) {
        throw (TestNGException) t;
      }
      throw new TestNGException(t);
    }
  }

  private Collection<XmlSuite> processCommandLineArgs(Collection<XmlSuite> allSuites) {
    Collection<XmlSuite> result = new ArrayList<>();
    for (XmlSuite s : allSuites) {
      processParallelModeCommandLineArgs(s);
      if (m_testNames == null) {
        result.add(s);
        continue;
      }
      // When test names are given, keep only the tests with those names
      TestNamesMatcher testNamesMatcher =
          new TestNamesMatcher(s, m_testNames, m_ignoreMissedTestNames);
      testNamesMatcher.validateMissMatchedTestNames();
      result.addAll(testNamesMatcher.getSuitesMatchingTestNames());
    }

    return result;
  }

  private void processParallelModeCommandLineArgs(XmlSuite suite) {
    if (this.m_parallelMode != null) {
      suite.setParallel(this.m_parallelMode);
    }
    if (this.m_threadCount > 0) {
      suite.setThreadCount(this.m_threadCount);
    }
    if (suite.getChildSuites() != null) {
      suite.getChildSuites().forEach(this::processParallelModeCommandLineArgs);
    }
  }

  /**
   * Builds the list of suites to run. Only the first call does the work.
   *
   * <p>When suites are already set, this method parses their {@code <suite-file>} tags. Otherwise,
   * it parses the suite files given by path, or it reads the suites from the test jar. The suite
   * files given by path win over the suite file inside the jar.
   */
  public void initializeSuitesAndJarFile() {
    // The IntelliJ plugin can call this method before run(). Do not build the suites twice.
    if (isSuiteInitialized) {
      return;
    }
    isSuiteInitialized = true;

    if (!m_suites.isEmpty()) {
      parseSuiteFiles(); // parse the <suite-file> tags, if any
      return;
    }

    //
    // Parse the suite files that were given by path
    //
    for (String suitePath : m_stringSuites) {
      Collection<XmlSuite> allSuites = parseSuite(suitePath);
      m_suites.addAll(processCommandLineArgs(allSuites));
    }

    //
    // The jar
    //
    // Suite files given by path win over the suite file inside the jar
    if (m_jarPath != null && !m_stringSuites.isEmpty()) {
      StringBuilder suites = new StringBuilder();
      for (String s : m_stringSuites) {
        suites.append(s);
      }
      Utils.log(
          "TestNG",
          2,
          "Ignoring the XML file inside " + m_jarPath + " and using " + suites + " instead");
      return;
    }
    if (isStringEmpty(m_jarPath)) {
      return;
    }

    // There is a jar, and no suite file was given. Look for a suite file inside the jar.
    File jarFile =
        new File(
            Objects.requireNonNull(m_jarPath, "a jar suite is only read once -testjar was given"));

    JarFileUtils utils =
        new JarFileUtils(
            getProcessor(), m_xmlPathInJar, m_testNames, m_parallelMode, m_ignoreMissedTestNames);

    Collection<XmlSuite> allSuites = utils.extractSuitesFrom(jarFile);
    allSuites.forEach(this::processParallelModeCommandLineArgs);
    m_suites.addAll(allSuites);
  }

  /**
   * Sets the number of threads that run tests in parallel.
   *
   * @param threadCount the number of threads, at least 1.
   * @throws TestNGException when {@code threadCount} is less than 1.
   */
  public void setThreadCount(int threadCount) {
    if (threadCount < 1) {
      // Throw, and do not stop the JVM. A setter that stops the JVM breaks every program that
      // embeds TestNG. Only TestNG#main may turn a bad value into an exit code.
      throw new TestNGException(
          "Cannot use a threadCount parameter less than 1; 1 > " + threadCount);
    }

    m_threadCount = threadCount;
  }

  /**
   * Sets the parallel mode, by name.
   *
   * @param parallel the name of the mode, such as {@code methods} or {@code classes}.
   * @deprecated Use {@link #setParallel(XmlSuite.ParallelMode)} instead.
   */
  @Deprecated
  // TODO: krmahadevan: Gradle uses this method. Removing it breaks the Gradle build.
  public void setParallel(String parallel) {
    setParallel(XmlSuite.ParallelMode.getValidParallel(parallel));
  }

  /**
   * Sets the parallel mode. It overrides the mode in the suite files.
   *
   * @param parallel the mode.
   */
  public void setParallel(XmlSuite.ParallelMode parallel) {
    m_parallelMode = parallel;
  }

  /**
   * Adds a suite, and treats it as a suite built from command line options.
   *
   * <p>TestNG applies the thread count, the parallel mode, the configuration failure policy and the
   * groups of this object to it.
   *
   * @param suite the suite.
   */
  public void setCommandLineSuite(XmlSuite suite) {
    m_cmdlineSuites = new ArrayList<>();
    m_cmdlineSuites.add(suite);
    m_suites.add(suite);
  }

  /**
   * Sets the test classes to run.
   *
   * <p>When the run starts, TestNG builds a suite around these classes. The suite and the test get
   * the default names, unless a class gives its own names in {@code @Test(suiteName, testName)}.
   * This method removes the suites that you set before as {@link XmlSuite} objects, for example
   * with {@link #setXmlSuites(List)}. It keeps the paths that you set with {@link
   * #setTestSuites(List)}, so TestNG runs those suite files too.
   *
   * @param classes the classes that hold TestNG annotations.
   */
  public void setTestClasses(Class[] classes) {
    m_suites.clear();
    m_commandLineTestClasses = classes;
  }

  /**
   * Splits a full method name, such as {@code com.example.Foo.f1}, into the class name and the
   * method name. A {@code *} in the method name becomes the regular expression {@code .*}.
   *
   * @throws TestNGException when the name has no dot.
   */
  private String[] splitMethod(String m) {
    int index = m.lastIndexOf(".");
    if (index < 0) {
      throw new TestNGException(
          "Bad format for command line method:" + m + ", expected <class>.<method>");
    }

    return new String[] {m.substring(0, index), m.substring(index + 1).replaceAll("\\*", "\\.\\*")};
  }

  /**
   * Builds the suites for a list of methods. Each class gets an {@code <include>} for each of its
   * methods in the list.
   *
   * @param commandLineMethods full method names, such as {@code com.example.Foo.f1}.
   * @return the suites.
   */
  private List<XmlSuite> createCommandLineSuitesForMethods(List<String> commandLineMethods) {
    //
    // Collect the classes of the methods
    //
    Set<Class> classes = new HashSet<>();
    for (String m : commandLineMethods) {
      Class c = ClassHelper.forName(splitMethod(m)[0]);
      if (c != null) {
        classes.add(c);
      }
    }

    List<XmlSuite> result = createCommandLineSuitesForClasses(classes.toArray(new Class[0]));

    //
    // Add an <include> for each method
    //
    List<XmlClass> xmlClasses = new ArrayList<>();
    for (XmlSuite s : result) {
      for (XmlTest t : s.getTests()) {
        xmlClasses.addAll(t.getClasses());
      }
    }

    for (XmlClass xc : xmlClasses) {
      for (String m : commandLineMethods) {
        String[] split = splitMethod(m);
        String className = split[0];
        if (xc.getName().equals(className)) {
          XmlInclude includedMethod = new XmlInclude(split[1]);
          xc.getIncludedMethods().add(includedMethod);
        }
      }
    }

    return result;
  }

  private List<XmlSuite> createCommandLineSuitesForClasses(Class[] classes) {
    //
    // A class can name its suite and its test in @Test(suiteName, testName). Put each class in the
    // suite and the test that it names, or in the default ones.
    //

    XmlClass[] xmlClasses =
        Arrays.stream(classes).map(clazz -> new XmlClass(clazz, true)).toArray(XmlClass[]::new);
    Map<String, XmlSuite> suites = new HashMap<>();
    IAnnotationFinder finder = m_configuration.getAnnotationFinder();

    for (int i = 0; i < classes.length; i++) {
      Class<?> c = classes[i];
      ITestAnnotation test = finder.findAnnotation(c, ITestAnnotation.class);
      String suiteName = getDefaultSuiteName();
      String testName = getDefaultTestName();
      if (test != null) {
        suiteName = defaultIfStringEmpty(test.getSuiteName(), suiteName);
        testName = defaultIfStringEmpty(test.getTestName(), testName);
      }
      XmlSuite xmlSuite = suites.get(suiteName);
      if (xmlSuite == null) {
        xmlSuite = new XmlSuite();
        xmlSuite.setName(suiteName);
        suites.put(suiteName, xmlSuite);
      }

      if (m_dataProviderThreadCount != null) {
        xmlSuite.setDataProviderThreadCount(m_dataProviderThreadCount);
      }
      XmlTest xmlTest = null;
      for (XmlTest xt : xmlSuite.getTests()) {
        if (testName.equals(xt.getName())) {
          xmlTest = xt;
          break;
        }
      }

      if (xmlTest == null) {
        xmlTest = new XmlTest(xmlSuite);
        xmlTest.setName(testName);
      }

      xmlTest.getXmlClasses().add(xmlClasses[i]);
    }

    return new ArrayList<>(suites.values());
  }

  /**
   * Adds a method selector, by class name.
   *
   * @param className the class name of an {@link IMethodSelector}. A {@code null} or empty name
   *     changes nothing.
   * @param priority the priority of the selector. TestNG asks the selectors in order of priority,
   *     lowest first.
   */
  public void addMethodSelector(@Nullable String className, int priority) {
    if (Strings.isNotNullAndNotEmpty(className)) {
      m_methodDescriptors.put(className, priority);
    }
  }

  /**
   * Adds a method selector, as a {@code <method-selector>} tag of a suite file does.
   *
   * @param selector the method selector.
   */
  public void addMethodSelector(XmlMethodSelector selector) {
    m_selectors.add(selector);
  }

  /**
   * Sets whether TestNG reports each data provider row of a test method as its own skip. This
   * applies when TestNG skips the method because of its dependencies.
   *
   * @param reportAllDataDrivenTestsAsSkipped whether to report each row as its own skip.
   */
  public void setReportAllDataDrivenTestsAsSkipped(boolean reportAllDataDrivenTestsAsSkipped) {
    this.m_configuration.setReportAllDataDrivenTestsAsSkipped(reportAllDataDrivenTestsAsSkipped);
  }

  /**
   * Tells if TestNG reports each data provider row of a skipped test method as its own skip.
   *
   * @return {@code true} when TestNG reports each row as its own skip.
   */
  public boolean getReportAllDataDrivenTestsAsSkipped() {
    return this.m_configuration.getReportAllDataDrivenTestsAsSkipped();
  }

  /** Makes TestNG report a data provider failure as a test failure. */
  public void propagateDataProviderFailureAsTestFailure() {
    this.m_configuration.propagateDataProviderFailureAsTestFailure();
  }

  /**
   * Tells if TestNG reports a data provider failure as a test failure.
   *
   * @return {@code true} when TestNG reports it as a test failure.
   */
  public boolean isPropagateDataProviderFailureAsTestFailure() {
    return this.m_configuration.isPropagateDataProviderFailureAsTestFailure();
  }

  /**
   * Sets whether the data providers of a suite share one thread pool.
   *
   * @param flag whether to share one thread pool.
   */
  public void shareThreadPoolForDataProviders(boolean flag) {
    this.m_configuration.shareThreadPoolForDataProviders(flag);
  }

  /**
   * Tells if the data providers of a suite share one thread pool.
   *
   * @return {@code true} when they share one thread pool.
   */
  public boolean isShareThreadPoolForDataProviders() {
    return this.m_configuration.isShareThreadPoolForDataProviders();
  }

  /**
   * Tells if the tests of a suite, with or without a data provider, share one thread pool.
   *
   * @return {@code true} when they share one thread pool.
   */
  public boolean useGlobalThreadPool() {
    return this.m_configuration.useGlobalThreadPool();
  }

  /**
   * Sets whether the tests of a suite, with or without a data provider, share one thread pool.
   *
   * @param flag whether to share one thread pool.
   */
  public void shouldUseGlobalThreadPool(boolean flag) {
    this.m_configuration.shouldUseGlobalThreadPool(flag);
  }

  /**
   * Sets whether TestNG creates the instances of a {@code @Factory} only when it needs them.
   *
   * <p>This setting covers the whole run. The {@code lazy-factory} attribute of a suite overrides
   * it, and {@code @Factory(lazy = ...)} overrides both. It applies only to a {@code @Factory} on a
   * constructor.
   *
   * @param flag {@code true} to create the instances late by default.
   */
  public void setLazyFactoryInstantiation(boolean flag) {
    this.m_configuration.setLazyFactoryInstantiation(flag);
  }

  /**
   * Tells if TestNG creates the instances of a {@code @Factory} only when it needs them, by
   * default.
   *
   * @return {@code true} when TestNG creates the instances late by default.
   */
  public boolean isLazyFactoryInstantiation() {
    return this.m_configuration.isLazyFactoryInstantiation();
  }

  /**
   * Returns what TestNG does with a test method whose data provider returns no rows.
   *
   * @return the behavior.
   */
  public EmptyDataProviderBehavior getEmptyDataProviderBehavior() {
    return this.m_configuration.getEmptyDataProviderBehavior();
  }

  /**
   * Sets what TestNG does with a test method whose data provider returns no rows.
   *
   * @param emptyDataProviderBehavior the behavior.
   */
  public void setEmptyDataProviderBehavior(EmptyDataProviderBehavior emptyDataProviderBehavior) {
    this.m_configuration.setEmptyDataProviderBehavior(emptyDataProviderBehavior);
  }

  /**
   * Sets the paths of the suite files to run.
   *
   * <p>TestNG parses the files when the run starts. When a file is missing, {@link #run()} throws a
   * {@link TestNGException}.
   *
   * @param suites the paths of one or more suite files. For example:
   *     <pre>
   * TestNG tng = new TestNG();
   * List&lt;String&gt; suites = new ArrayList&lt;&gt;();
   * suites.add("c:/tests/testng1.xml");
   * suites.add("c:/tests/testng2.xml");
   * tng.setTestSuites(suites);
   * tng.run();
   * </pre>
   */
  public void setTestSuites(List<String> suites) {
    m_stringSuites = suites;
  }

  /**
   * Sets the suites to run, as {@link XmlSuite} objects.
   *
   * <p>TestNG uses the list that you pass, not a copy.
   *
   * @param suites the suites.
   */
  public void setXmlSuites(List<XmlSuite> suites) {
    m_suites = suites;
  }

  /**
   * Sets the groups to leave out of the run. They override the groups in the suite files.
   *
   * @param groups the group names, separated by commas.
   */
  public void setExcludedGroups(@Nullable String groups) {
    m_excludedGroups = Utils.split(groups, ",");
  }

  /**
   * Sets the groups to run. They override the groups in the suite files.
   *
   * @param groups the group names, separated by commas.
   */
  public void setGroups(@Nullable String groups) {
    m_includedGroups = Utils.split(groups, ",");
  }

  /**
   * Sets the factory that creates the {@link TestRunner} of each {@code <test>}, by class.
   *
   * <p>TestNG creates the factory with the object factory that it uses at the time of the call.
   *
   * @param testRunnerFactoryClass the class of the factory.
   */
  public void setTestRunnerFactoryClass(
      Class<? extends ITestRunnerFactory> testRunnerFactoryClass) {
    setTestRunnerFactory(m_objectFactory.newInstance(testRunnerFactoryClass));
  }

  /**
   * Sets the factory that creates the {@link TestRunner} of each {@code <test>}.
   *
   * @param itrf the factory.
   */
  protected void setTestRunnerFactory(ITestRunnerFactory itrf) {
    m_testRunnerFactory = itrf;
  }

  /**
   * Sets the object factory, by class.
   *
   * <p>TestNG creates the new factory with the object factory that it uses at the time of the call.
   *
   * @param c the class of the object factory.
   */
  public void setObjectFactory(Class<? extends ITestObjectFactory> c) {
    setObjectFactory(m_objectFactory.newInstance(c));
  }

  /**
   * Sets the factory that TestNG uses to create objects, such as the instances of test classes.
   *
   * @param factory the object factory.
   */
  public void setObjectFactory(ITestObjectFactory factory) {
    m_objectFactory = factory;
  }

  /**
   * Sets the listener classes of the run.
   *
   * <p>With a listener factory, TestNG creates the listeners at once. Without one, TestNG creates
   * them when it has a suite, so that a listener can use the Guice parent module of that suite.
   *
   * @param classes the listener classes. Each one implements {@link ITestNGListener}.
   */
  public void setListenerClasses(List<Class<? extends ITestNGListener>> classes) {
    ITestNGListenerFactory factory = m_configuration.getListenerFactory();
    if (factory == null) {
      // The command line configure() calls this before setTestSuites(), so m_suites is still
      // empty. Keep the classes, and create the listeners when a suite exists. Then a -listener
      // can use the Guice parent module of that suite.
      m_listenerClasses.addAll(classes);
      if (!m_suites.isEmpty()) {
        instantiatePendingListenerClasses();
      }
      return;
    }
    for (Class<? extends ITestNGListener> cls : classes) {
      ITestNGListener created = factory.createListener(cls);
      if (created != null) {
        addListener(created);
      }
    }
  }

  private void instantiatePendingListenerClasses() {
    if (m_listenerClasses.isEmpty()) {
      return;
    }
    XmlSuite suite = m_suites.isEmpty() ? new XmlSuite() : m_suites.get(0);
    IObjectDispenser dispenser = Dispenser.newInstance(m_objectFactory);
    GuiceContext context = new GuiceContext(suite, this.m_configuration);
    for (Class<? extends ITestNGListener> cls : m_listenerClasses) {
      BasicAttributes basic = new BasicAttributes(null, cls);
      CreationAttributes attributes = new CreationAttributes(basic, context);
      Object created = dispenser.dispense(attributes);
      if (created != null) {
        addListener((ITestNGListener) created);
      }
    }
    m_listenerClasses.clear();
  }

  /**
   * Adds a listener of any type.
   *
   * <p>When the object is not an {@link ITestNGListener}, this method throws a
   * {@link TestNGException}.
   *
   * @param listener the listener to add.
   * @throws TestNGException if {@code listener} is not an {@link ITestNGListener}.
   * @deprecated Use {@link #addListener(ITestNGListener)} instead.
   */
  // TODO: remove this method later. Caution: IntelliJ uses it. Check with @akozlova first.
  @Deprecated
  public void addListener(Object listener) {
    if (!(listener instanceof ITestNGListener)) {
      // A setter throws. Only TestNG#main turns a bad value into an exit code.
      throw new TestNGException(
          "Listener "
              + listener
              + " must be one of ITestListener, ISuiteListener, IReporter, "
              + " IAnnotationTransformer, IMethodInterceptor or IInvokedMethodListener");
    }
    addListener((ITestNGListener) listener);
  }

  private static <E> void maybeAddListener(Map<Class<? extends E>, E> map, E value) {
    maybeAddListener(map, (Class<? extends E>) value.getClass(), value, false);
  }

  private static <E> void maybeAddListener(
      Map<Class<? extends E>, E> map, Class<? extends E> type, E value, boolean quiet) {
    if (map.putIfAbsent(type, value) != null && !quiet) {
      LOGGER.warn("Ignoring duplicate listener : " + type.getName());
    }
  }

  /**
   * Adds a listener to the run.
   *
   * <p>A listener can implement several listener interfaces. TestNG adds it for each of them. For
   * most interfaces, TestNG keeps one listener of each class. It ignores a second listener of the
   * same class, and logs a warning. An {@link IAnnotationTransformer} replaces the transformer that
   * TestNG had before.
   *
   * <p>TestNG ignores a {@code null} listener, and a listener whose {@link
   * ITestNGListener#isEnabled()} returns {@code false}.
   *
   * @param listener the listener to add.
   */
  public void addListener(ITestNGListener listener) {
    if (listener == null) {
      return;
    }
    if (!listener.isEnabled()) {
      return;
    }
    if (listener instanceof IExecutionVisualiser) {
      IExecutionVisualiser visualiser = (IExecutionVisualiser) listener;
      maybeAddListener(m_executionVisualisers, visualiser);
    }
    if (listener instanceof ISuiteListener) {
      ISuiteListener suite = (ISuiteListener) listener;
      maybeAddListener(m_suiteListeners, suite);
    }
    if (listener instanceof ITestListener) {
      ITestListener test = (ITestListener) listener;
      maybeAddListener(m_testListeners, test);
    }
    if (listener instanceof IClassListener) {
      IClassListener clazz = (IClassListener) listener;
      maybeAddListener(m_classListeners, clazz);
    }
    if (listener instanceof IReporter) {
      IReporter reporter = (IReporter) listener;
      maybeAddListener(m_reporters, reporter);
    }
    if (listener instanceof IAnnotationTransformer) {
      setAnnotationTransformer((IAnnotationTransformer) listener);
    }
    if (listener instanceof IMethodInterceptor) {
      m_methodInterceptors.add((IMethodInterceptor) listener);
    }
    if (listener instanceof IInvokedMethodListener) {
      IInvokedMethodListener method = (IInvokedMethodListener) listener;
      maybeAddListener(m_invokedMethodListeners, method);
    }
    if (listener instanceof IHookable) {
      setHookable((IHookable) listener);
    }
    if (listener instanceof IConfigurable) {
      setConfigurable((IConfigurable) listener);
    }
    if (listener instanceof IExecutionListener) {
      m_configuration.addExecutionListenerIfAbsent((IExecutionListener) listener);
    }
    if (listener instanceof IConfigurationListener) {
      m_configuration.addConfigurationListener((IConfigurationListener) listener);
    }
    if (listener instanceof IAlterSuiteListener) {
      IAlterSuiteListener alter = (IAlterSuiteListener) listener;
      maybeAddListener(m_alterSuiteListeners, alter);
    }
    if (listener instanceof IDataProviderListener) {
      IDataProviderListener dataProvider = (IDataProviderListener) listener;
      maybeAddListener(m_dataProviderListeners, dataProvider);
    }
    if (listener instanceof IDataProviderInterceptor) {
      IDataProviderInterceptor interceptor = (IDataProviderInterceptor) listener;
      maybeAddListener(m_dataProviderInterceptors, interceptor);
    }
    if (listener instanceof IParameterResolver) {
      IParameterResolver resolver = (IParameterResolver) listener;
      maybeAddListener(m_parameterResolvers, resolver);
    }
  }

  /**
   * Returns the reporters of the run.
   *
   * @return a copy, in no set order.
   */
  public Set<IReporter> getReporters() {
    // Return a copy. TestNG keeps the reporters in a map, by class, so there is no set to share.
    return new HashSet<>(m_reporters.values());
  }

  /**
   * Returns the test listeners of the run.
   *
   * @return a copy, in the order in which they were added.
   */
  public List<ITestListener> getTestListeners() {
    return new ArrayList<>(m_testListeners.values());
  }

  /**
   * Returns the suite listeners of the run.
   *
   * @return a copy, in the order in which they were added.
   */
  public List<ISuiteListener> getSuiteListeners() {
    return new ArrayList<>(m_suiteListeners.values());
  }

  /** The verbose level. When it is set, it overrides the verbose level of the suite files. */
  private @Nullable Integer m_verbose = null;

  private final IAnnotationTransformer m_defaultAnnoProcessor = new DefaultAnnotationTransformer();
  private IAnnotationTransformer m_annotationTransformer = m_defaultAnnoProcessor;

  private @Nullable Boolean m_skipFailedInvocationCounts = false;

  private final List<IMethodInterceptor> m_methodInterceptors = new ArrayList<>();

  /** The names of the {@code <test>} tags to run from the suites. */
  private @Nullable List<String> m_testNames;

  private boolean m_ignoreMissedTestNames;

  private Integer m_suiteThreadPoolSize = DEFAULT_SUITE_THREAD_POOL_SIZE;

  private boolean m_randomizeSuites = false;

  private boolean m_alwaysRun = true;

  private Boolean m_preserveOrder = XmlSuite.DEFAULT_PRESERVE_ORDER;
  private @Nullable Boolean m_groupByInstances;
  private boolean m_generateResultsPerSuite = false;

  private IConfiguration m_configuration;

  /**
   * Sets the verbose level. It overrides the level in the suite files.
   *
   * @param verbose the level, from 0 to 10, where 10 prints the most detail. The value -1 puts
   *     TestNG in debug mode, which prints full stack traces.
   */
  public void setVerbose(int verbose) {
    m_verbose = verbose;
  }

  /**
   * Sets the factory that creates the thread pools of TestNG.
   *
   * @param factory the factory.
   * @throws NullPointerException when {@code factory} is {@code null}.
   */
  public void setExecutorServiceFactory(IExecutorServiceFactory factory) {
    m_configuration.setExecutorServiceFactory(
        Objects.requireNonNull(factory, "ExecutorServiceFactory cannot be null"));
  }

  /**
   * Sets the factory that creates the thread pools of TestNG, by class.
   *
   * <p>TestNG creates the factory with the object factory that it uses at the time of the call.
   *
   * @param factoryClass the class of the factory.
   */
  public void setExecutorServiceFactoryClass(
      Class<? extends IExecutorServiceFactory> factoryClass) {
    setExecutorServiceFactory(m_objectFactory.newInstance(factoryClass));
  }

  /**
   * Sets the factory that creates the listeners of {@link #setListenerClasses(List)}.
   *
   * @param factory the listener factory.
   */
  public void setListenerFactory(ITestNGListenerFactory factory) {
    this.m_configuration.setListenerFactory(factory);
  }

  /**
   * Sets the factory that creates the listeners, by class.
   *
   * <p>TestNG creates the factory with the object factory that it uses at the time of the call.
   *
   * @param factoryClass the class of the factory.
   */
  public void setListenerFactoryClass(Class<? extends ITestNGListenerFactory> factoryClass) {
    setListenerFactory(m_objectFactory.newInstance(factoryClass));
  }

  /**
   * Sets whether TestNG writes the results of each suite to its own directory.
   *
   * @param generateResultsPerSuite whether to use one directory for each suite.
   */
  public void setGenerateResultsPerSuite(boolean generateResultsPerSuite) {
    this.m_generateResultsPerSuite = generateResultsPerSuite;
  }

  private void initializeCommandLineSuites() {
    if (m_commandLineTestClasses != null || m_commandLineMethods != null) {
      List<String> cliMethods = m_commandLineMethods;
      Class<?>[] cliClasses = m_commandLineTestClasses;
      m_cmdlineSuites =
          cliMethods != null
              ? createCommandLineSuitesForMethods(cliMethods)
              : createCommandLineSuitesForClasses(
                  Objects.requireNonNull(cliClasses, "one of the two command line inputs is set"));

      for (XmlSuite s : m_cmdlineSuites) {
        for (XmlTest t : s.getTests()) {
          t.setPreserveOrder(m_preserveOrder);
        }
        m_suites.add(s);
        if (m_groupByInstances != null) {
          s.setGroupByInstances(m_groupByInstances);
        }
      }
    }
  }

  private void initializeCommandLineSuitesParams() {
    if (null == m_cmdlineSuites) {
      return;
    }

    for (XmlSuite s : m_cmdlineSuites) {
      if (m_threadCount != -1) {
        s.setThreadCount(m_threadCount);
      }
      if (m_parallelMode != null) {
        s.setParallel(m_parallelMode);
      }
      if (m_configFailurePolicy != null) {
        s.setConfigFailurePolicy(m_configFailurePolicy);
      }
    }
  }

  private void initializeCommandLineSuitesGroups() {
    // Groups given to this object override the groups in the suite files
    List<XmlSuite> suites = m_cmdlineSuites != null ? m_cmdlineSuites : m_suites;
    for (XmlSuite s : suites) {
      initializeCommandLineSuitesGroups(s, m_includedGroups, m_excludedGroups);
    }
  }

  private static void initializeCommandLineSuitesGroups(
      XmlSuite s, String @Nullable [] included, String @Nullable [] excluded) {
    if (included != null && included.length > 0) {
      s.setIncludedGroups(Arrays.asList(included));
    }
    if (excluded != null && excluded.length > 0) {
      s.setExcludedGroups(Arrays.asList(excluded));
    }
    for (XmlSuite child : s.getChildSuites()) {
      initializeCommandLineSuitesGroups(child, included, excluded);
    }
  }

  private void addReporter(Class<? extends IReporter> r) {
    if (!m_reporters.containsKey(r)) {
      m_reporters.put(r, m_objectFactory.newInstance(r));
    }
  }

  private void initializeDefaultListeners() {
    if (m_failIfAllTestsSkipped) {
      this.exitCodeListener.failIfAllTestsSkipped();
    }
    if (m_useDefaultListeners) {
      addReporter(Main.class);
      addReporter(FailedReporter.class);
      if (m_generateResultsPerSuite) {
        addReporter(PerSuiteXMLReporter.class);
      } else {
        addReporter(XMLReporter.class);
      }
      if (RuntimeBehavior.useEmailableReporter()) {
        addReporter(EmailableReporter2.class);
      }
      addReporter(JUnitReportReporter.class);
      if (m_verbose != null && m_verbose > 4) {
        addListener(new VerboseReporter("[TestNG] "));
      }
    }
  }

  // Compare with == on purpose, in the one check that is not a null check. DEFAULT_OBJECT_FACTORY
  // is a marker that means "no suite has named a factory yet". A suite that names one replaces it
  // with an object of a user class, and TestNG does not own the equals() of that class.
  @SuppressWarnings("ReferenceEquality")
  private void initializeConfiguration() {
    ITestObjectFactory factory = m_objectFactory;
    //
    // Add the listeners that ServiceLoader finds. Use the class loader for tests, when one is set.
    //
    addServiceLoaderListeners();
    instantiatePendingListenerClasses();

    //
    // Add the listeners that the suites name
    //
    for (XmlSuite s : m_suites) {
      addListeners(s);

      //
      // Add the method selectors of the suite
      //
      for (XmlMethodSelector methodSelector : s.getMethodSelectors()) {
        addMethodSelector(methodSelector.getClassName(), methodSelector.getPriority());
        addMethodSelector(methodSelector);
      }

      //
      // Use the object factory that a suite names, if any. Only one suite may name one.
      //
      if (s.getObjectFactoryClass() != null) {
        if (factory != DEFAULT_OBJECT_FACTORY) {
          throw new TestNGException("Found more than one object-factory tag in your suites");
        }
        factory = m_objectFactory.newInstance(s.getObjectFactoryClass());
      }
    }

    m_configuration.setAnnotationFinder(new JDK15AnnotationFinder(getAnnotationTransformer()));
    m_configuration.setHookable(m_hookable);
    m_configuration.setConfigurable(m_configurable);
    m_configuration.setObjectFactory(factory);
    m_configuration.setAlwaysRunListeners(this.m_alwaysRun);
  }

  private void addListeners(XmlSuite s) {
    IObjectDispenser dispenser = Dispenser.newInstance(m_objectFactory);
    GuiceContext context = new GuiceContext(s, this.m_configuration);
    for (String listenerName : s.getListeners()) {
      Class<?> listenerClass = ClassHelper.forName(listenerName);

      // Fail when the listener class is not on the class path
      if (listenerClass == null) {
        throw new TestNGException(
            "Listener " + listenerName + " was not found in project's classpath");
      }

      BasicAttributes basic = new BasicAttributes(null, listenerClass);
      CreationAttributes attribute = new CreationAttributes(basic, context);
      Object listener = dispenser.dispense(attribute);
      if (listener != null) {
        addListener((ITestNGListener) listener);
      }
    }

    // Add the listeners of the child suites
    List<XmlSuite> childSuites = s.getChildSuites();
    for (XmlSuite c : childSuites) {
      addListeners(c);
    }
  }

  /** Adds the listeners that {@link ServiceLoader} finds, except the ones to skip. */
  private void addServiceLoaderListeners() {
    Iterable<ITestNGListener> loader =
        m_serviceLoaderClassLoader != null
            ? ServiceLoader.load(ITestNGListener.class, m_serviceLoaderClassLoader)
            : ServiceLoader.load(ITestNGListener.class);
    for (ITestNGListener l : loader) {
      Utils.log("[TestNG]", 2, "Adding ServiceLoader listener:" + l);
      if (m_listenersToSkipFromBeingWiredIn.contains(l.getClass().getName())) {
        Utils.log("[TestNG]", 2, "Skipping adding the listener :" + l);
        continue;
      }
      addListener(l);
      addServiceLoaderListener(l);
    }
  }

  /**
   * Checks the suites before they run.
   *
   * <p>It fails when a suite has two tests with the same name. It also renames suites, so that each
   * suite name is unique.
   *
   * @throws TestNGException when a check fails.
   */
  private void sanityCheck() {
    XmlSuiteUtils.validateIfSuitesContainDuplicateTests(m_suites);
    XmlSuiteUtils.adjustSuiteNamesToEnsureUniqueness(m_suites);
  }

  /**
   * Prepares the suites, the configuration, the default listeners and the command line suites of
   * the run. Only the first call does the work.
   *
   * <p>{@link #run()} calls this method. The remote runner of the Eclipse plugin can call it before
   * that.
   */
  public void initializeEverything() {
    // The Eclipse plugin (RemoteTestNG) can call this method before run(). Do not prepare twice.
    if (m_isInitialized) {
      return;
    }

    initializeSuitesAndJarFile();
    initializeConfiguration();
    initializeDefaultListeners();
    initializeCommandLineSuites();
    initializeCommandLineSuitesParams();
    initializeCommandLineSuitesGroups();

    m_isInitialized = true;
  }

  /**
   * Runs the suites and writes the reports.
   *
   * <p>It calls the {@link IExecutionListener} listeners before and after the run. It calls the
   * {@link IAlterSuiteListener} listeners before the suites run. After this method returns, {@link
   * #getStatus()} gives the result.
   */
  public void run() {
    initializeEverything();
    sanityCheck();

    runExecutionListeners(true /* start */);

    runSuiteAlterationListeners();

    m_start = System.currentTimeMillis();
    List<ISuite> suiteRunners = runSuites();

    m_end = System.currentTimeMillis();

    if (null != suiteRunners) {
      suiteRunners.forEach(ObjectBag::cleanup);
      try {
        generateReports(suiteRunners);
      } finally {
        // The reporters are the last readers of the parameter snapshots.
        suiteRunners.forEach(ParameterSnapshots::detachFrom);
      }
    }

    runExecutionListeners(false /* finish */);
    exitCode = this.exitCodeListener.getStatus();

    if (exitCodeListener.noTestsFound()) {
      if (TestRunner.getVerbose() > 1) {
        System.err.println("[TestNG] No tests found. Nothing was run");
        usage();
      }
    }

    m_instance = null;
  }

  /**
   * Runs the suites.
   *
   * <p>A subclass can override this method, for example to run the suites on other machines.
   *
   * @return the suites that ran.
   * @since 6.9.11
   */
  protected List<ISuite> runSuites() {
    return runSuitesLocally();
  }

  private void runSuiteAlterationListeners() {
    Collection<IAlterSuiteListener> original =
        sort(m_alterSuiteListeners.values(), m_configuration.getListenerComparator());
    for (IAlterSuiteListener l : original) {
      l.alter(m_suites);
    }
  }

  private void runExecutionListeners(boolean start) {
    List<IExecutionListener> executionListeners =
        ListenerOrderDeterminer.order(
            m_configuration.getExecutionListeners(), m_configuration.getListenerComparator());
    if (start) {
      for (IExecutionListener l : executionListeners) {
        l.onExecutionStart();
      }
      // Call the exit code listener of TestNG after all the user listeners.
      exitCodeListener.onExecutionStart();
    } else {
      List<IExecutionListener> executionListenersReversed =
          ListenerOrderDeterminer.reversedOrder(
              m_configuration.getExecutionListeners(), m_configuration.getListenerComparator());
      for (IExecutionListener l : executionListenersReversed) {
        l.onExecutionFinish();
      }
      // Call the exit code listener of TestNG after all the user listeners.
      exitCodeListener.onExecutionFinish();
    }
  }

  private static void usage() {
    // Use find(), not required(). Printing the usage text must never fail.
    ITestNGCliRunner runner = CliRunners.find();
    if (runner != null) {
      try {
        runner.usage();
        return;
      } catch (RuntimeException ignored) {
        // A runner from another vendor must not break the callers of usage(). Those include plain
        // Java API paths, such as runSuitesLocally(). Print the built-in text instead.
      }
    }
    // Print to standard output, as a command line runner does.
    System.out.println(
        "Usage: java "
            + TestNG.class.getName()
            + " [options] <testng.xml> ...\n"
            + "Detailed command line help requires an "
            + ITestNGCliRunner.class.getName()
            + " implementation on the classpath; the org.testng:testng jar bundles one.");
  }

  private void generateReports(List<ISuite> suiteRunners) {
    List<IReporter> reporters = new ArrayList<>(m_reporters.values());
    // Add the exit code listener as the last reporter. Then it sees any change that a user
    // reporter made.
    reporters.add(exitCodeListener);
    for (IReporter reporter : reporters) {
      try {
        long start = System.currentTimeMillis();
        reporter.generateReport(m_suites, suiteRunners, m_outputDir);
        Utils.log(
            "TestNG",
            2,
            "Time taken by " + reporter + ": " + (System.currentTimeMillis() - start) + " ms");
      } catch (Exception ex) {
        System.err.println("[TestNG] Reporter " + reporter + " failed");
        ex.printStackTrace(System.err);
      }
    }
  }

  /**
   * Runs the suites in this JVM, in sequence or in parallel.
   *
   * <p>This method is public because Maven 2 calls it.
   *
   * @return the suites that ran. When there is no suite, TestNG logs an error, prints the usage
   *     text, and returns an empty list.
   */
  public List<ISuite> runSuitesLocally() {
    if (m_suites.isEmpty()) {
      error("No test suite found. Nothing to run");
      usage();
      return new ArrayList<>();
    }

    SuiteRunnerMap suiteRunnerMap = new SuiteRunnerMap();

    if (m_suites.get(0).getVerbose() >= 2) {
      Version.displayBanner();
    }

    // Create all the suite runners first, so that TestNG finds a configuration problem before any
    // suite runs. The map gives the runner of each suite.
    for (XmlSuite xmlSuite : m_suites) {
      if (m_configuration.isShareThreadPoolForDataProviders()) {
        xmlSuite.setShareThreadPoolForDataProviders(true);
      }
      if (m_configuration.useGlobalThreadPool()) {
        xmlSuite.shouldUseGlobalThreadPool(true);
      }
      createSuiteRunners(suiteRunnerMap, xmlSuite);
    }

    // TestNG knows every reporter of the run by now, including the ones that the suites named. No
    // suite has started yet, and a reporter must ask for parameter snapshots before that. The
    // reporters of a TestRunner ask for snapshots themselves. See ParameterSnapshotReader.
    ParameterSnapshotReader.requestCaptureIfAnyReads(m_reporters.values(), suiteRunnerMap.values());

    //
    // Run the suites
    //
    if (m_suiteThreadPoolSize == 1 && !m_randomizeSuites) {
      // With one thread and no random order, run the suites in order
      for (XmlSuite xmlSuite : m_suites) {
        runSuitesSequentially(
            xmlSuite, suiteRunnerMap, getVerbose(xmlSuite), getDefaultSuiteName());
      }
      //
      // Return the suite runners. run() writes the reports.
      //
      return new ArrayList<>(suiteRunnerMap.values());
    }
    // With more than one thread, or with random order, build a graph of the suites. A parent suite
    // runs only after all its child suites finish.
    IDynamicGraph<ISuite> suiteGraph = new DynamicGraph<>();
    for (XmlSuite xmlSuite : m_suites) {
      populateSuiteGraph(suiteGraph, suiteRunnerMap, xmlSuite);
    }

    IThreadWorkerFactory<ISuite> factory =
        new SuiteWorkerFactory(
            suiteRunnerMap, 0 /* verbose hasn't been set yet */, getDefaultSuiteName());
    SuiteTaskExecutor taskExecutor =
        new SuiteTaskExecutor(
            this.m_configuration,
            factory,
            new LinkedBlockingQueue<>(),
            suiteGraph,
            m_suiteThreadPoolSize);
    taskExecutor.execute();
    taskExecutor.awaitCompletion();

    //
    // Return the suite runners. run() writes the reports.
    //
    return new ArrayList<>(suiteRunnerMap.values());
  }

  private static void error(@Nullable String s) {
    LOGGER.error(s);
  }

  /**
   * Returns the verbose level for a suite.
   *
   * <p>It takes the level of the suite, then the level of this object, then the default level. The
   * {@code testng.default.verbose} system property sets the default level, which is 1 without it.
   *
   * @return the verbose level.
   */
  private int getVerbose(XmlSuite xmlSuite) {
    return xmlSuite.getVerbose() != null
        ? xmlSuite.getVerbose()
        : (m_verbose != null ? m_verbose : RuntimeBehavior.getDefaultVerboseLevel());
  }

  /**
   * Runs a suite after its child suites.
   *
   * <p>The child suites run first, so that the results of the parent suite can include theirs.
   *
   * @param xmlSuite the suite to run.
   * @param suiteRunnerMap the map that gives the runner of each suite.
   * @param verbose the verbose level.
   * @param defaultSuiteName the name to print for a suite with no file.
   */
  private void runSuitesSequentially(
      XmlSuite xmlSuite, SuiteRunnerMap suiteRunnerMap, int verbose, String defaultSuiteName) {
    for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
      runSuitesSequentially(childSuite, suiteRunnerMap, verbose, defaultSuiteName);
    }
    SuiteRunnerWorker srw =
        new SuiteRunnerWorker(
            suiteRunnerMap.require(xmlSuite), suiteRunnerMap, verbose, defaultSuiteName);
    srw.run();
  }

  /**
   * Adds a suite and its child suites to the graph.
   *
   * <p>In the graph, each parent suite depends on its child suites, so the child suites run first.
   *
   * @param suiteGraph the graph to fill.
   * @param suiteRunnerMap the map that gives the runner of each suite.
   * @param xmlSuite the suite to add.
   */
  private void populateSuiteGraph(
      IDynamicGraph<ISuite> suiteGraph /* OUT */,
      SuiteRunnerMap suiteRunnerMap,
      XmlSuite xmlSuite) {
    ISuite parentSuiteRunner = suiteRunnerMap.require(xmlSuite);
    suiteGraph.addNode(parentSuiteRunner);
    if (!xmlSuite.getChildSuites().isEmpty()) {
      for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
        suiteGraph.addEdge(0, parentSuiteRunner, suiteRunnerMap.require(childSuite));
        populateSuiteGraph(suiteGraph, suiteRunnerMap, childSuite);
      }
    }
  }

  /**
   * Creates the runners of a suite and of its child suites, and adds them to the map.
   *
   * <p>First, this method copies settings of this object into the suite, such as the verbose level.
   *
   * @param suiteRunnerMap the map that gives the runner of each suite. This method adds to it.
   * @param xmlSuite the suite.
   */
  private void createSuiteRunners(SuiteRunnerMap suiteRunnerMap /* OUT */, XmlSuite xmlSuite) {
    // A skip setting on this object overrides the setting of the suite. Note: the default value,
    // false, is not null, so it overrides the suite too.
    if (null != m_skipFailedInvocationCounts) {
      xmlSuite.setSkipFailedInvocationCounts(m_skipFailedInvocationCounts);
    }

    // Override the verbose level of the suite with the one of this object
    if (m_verbose != null) {
      xmlSuite.setVerbose(m_verbose);
    }

    if (null != m_configFailurePolicy) {
      xmlSuite.setConfigFailurePolicy(m_configFailurePolicy);
    }

    if (null != m_dataProviderThreadCount) {
      xmlSuite.setDataProviderThreadCount(m_dataProviderThreadCount);
    }

    Set<XmlMethodSelector> selectors = new HashSet<>();
    for (XmlTest t : xmlSuite.getTests()) {
      for (Map.Entry<String, Integer> ms : m_methodDescriptors.entrySet()) {
        XmlMethodSelector xms = new XmlMethodSelector();
        xms.setName(ms.getKey());
        xms.setPriority(ms.getValue());
        selectors.add(xms);
      }
      selectors.addAll(m_selectors);
      t.getMethodSelectors().addAll(new ArrayList<>(selectors));
    }

    suiteRunnerMap.put(xmlSuite, createSuiteRunner(xmlSuite));

    for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
      createSuiteRunners(suiteRunnerMap, childSuite);
    }
  }

  /** Creates the runner of a suite, with the listeners of this object. */
  private SuiteRunner createSuiteRunner(XmlSuite xmlSuite) {
    DataProviderHolder holder = new DataProviderHolder(m_configuration);
    holder.addListeners(m_dataProviderListeners.values());
    holder.addInterceptors(m_dataProviderInterceptors.values());
    TestListenersContainer container =
        new TestListenersContainer(getTestListeners(), this.exitCodeListener);
    SuiteRunner result =
        new SuiteRunner(
            getConfiguration(),
            xmlSuite,
            m_outputDir,
            m_testRunnerFactory,
            m_useDefaultListeners,
            m_methodInterceptors,
            m_invokedMethodListeners.values(),
            container,
            m_classListeners.values(),
            holder,
            MethodSorting.basedOn());

    result.addParameterResolvers(m_parameterResolvers.values());

    for (ISuiteListener isl : m_suiteListeners.values()) {
      result.addListener(isl);
    }

    for (IReporter r : result.getReporters()) {
      maybeAddListener(m_reporters, r.getClass(), r, true);
    }

    for (IConfigurationListener cl : m_configuration.getConfigurationListeners()) {
      result.addConfigurationListener(cl);
    }

    m_executionVisualisers.values().forEach(result::addListener);

    return result;
  }

  /**
   * Returns the configuration of the run.
   *
   * @return the configuration.
   */
  protected IConfiguration getConfiguration() {
    return m_configuration;
  }

  /**
   * Runs TestNG from the command line, then stops the JVM with the exit status of the run.
   *
   * <p>It passes {@code argv} to the {@link ITestNGCliRunner} on the class path. So {@code java -cp
   * testng.jar org.testng.TestNG suite.xml} still works.
   *
   * @param argv the TestNG command line arguments.
   * @deprecated since 7.13. The command line code is in its own modules. Call {@code
   *     org.testng.cli.jcommander.JCommanderCliRunner}, or another {@link ITestNGCliRunner},
   *     directly. Or use the Java API of TestNG. TestNG 8.0 will remove this method.
   */
  @Deprecated
  public static void main(String[] argv) {
    TestNG testng;
    try {
      testng = privateMain(argv, null);
    } catch (TestNGException ex) {
      // Usually there is no ITestNGCliRunner on the class path. Report it as any other command
      // line error, not as an uncaught stack trace.
      exitWithError(ex.getMessage());
      return;
    }
    System.exit(testng.getStatus());
  }

  /**
   * Runs TestNG for a command line, without stopping the JVM.
   *
   * <p><b>Note</b>: This method is for the use of TestNG only. It is not part of the public API.
   *
   * @param argv the TestNG command line arguments.
   * @param listener a listener that TestNG adds before the run, or {@code null}.
   * @return the TestNG object that ran.
   * @throws TestNGException when no {@link ITestNGCliRunner} is on the class path, or when the
   *     runner cannot use {@code argv}. Since 7.13, a bad command line does not stop the JVM here.
   *     Only {@link #main(String[])} does that.
   * @deprecated since 7.13. Use {@link ITestNGCliRunner#run(String[], ITestListener)} on the runner
   *     of your choice. TestNG 8.0 will remove this method.
   */
  @Deprecated
  public static TestNG privateMain(String[] argv, @Nullable ITestListener listener) {
    return CliRunners.required().run(argv, listener);
  }

  /**
   * Sets up this object from command line values.
   *
   * @param cla the command line values.
   * @deprecated since 7.13. The command line code is in the {@code testng-cli} module. Use {@code
   *     org.testng.cli.CliConfigurer#configure(TestNG, org.testng.cli.CliOptions)}. This method no
   *     longer changes, so that subclasses such as {@code RemoteTestNG} keep working. TestNG 8.0
   *     will remove it.
   */
  @Deprecated
  protected void configure(CommandLineArgs cla) {
    Optional.ofNullable(cla.useGlobalThreadPool).ifPresent(this::shouldUseGlobalThreadPool);
    Optional.ofNullable(cla.shareThreadPoolForDataProviders)
        .ifPresent(this::shareThreadPoolForDataProviders);
    Optional.ofNullable(cla.propagateDataProviderFailureAsTestFailure)
        .ifPresent(value -> propagateDataProviderFailureAsTestFailure());
    setReportAllDataDrivenTestsAsSkipped(cla.includeAllDataDrivenTestsWhenSkipping);
    Optional.ofNullable(cla.emptyDataProviderBehavior)
        .ifPresent(this::setEmptyDataProviderBehavior);

    Optional.ofNullable(cla.listenerFactory)
        .map(ClassHelper::forName)
        .filter(ITestNGListenerFactory.class::isAssignableFrom)
        .map(it -> m_objectFactory.newInstance(it))
        .map(it -> (ITestNGListenerFactory) it)
        .ifPresent(this::setListenerFactory);

    Optional.ofNullable(cla.generateResultsPerSuite).ifPresent(this::setGenerateResultsPerSuite);

    Optional.ofNullable(cla.listenerComparator)
        .map(ClassHelper::forName)
        .filter(ListenerComparator.class::isAssignableFrom)
        .map(it -> m_objectFactory.newInstance(it))
        .map(it -> (ListenerComparator) it)
        .ifPresent(this::setListenerComparator);

    if (cla.verbose != null) {
      setVerbose(cla.verbose);
    }
    if (cla.dependencyInjectorFactoryClass != null) {
      Class<?> clazz = ClassHelper.forName(cla.dependencyInjectorFactoryClass);
      if (clazz != null && IInjectorFactory.class.isAssignableFrom(clazz)) {
        m_configuration.setInjectorFactory(
            m_objectFactory.newInstance((Class<IInjectorFactory>) clazz));
      }
    }
    Optional.ofNullable(cla.threadPoolFactoryClass)
        .map(ClassHelper::forName)
        .filter(IExecutorServiceFactory.class::isAssignableFrom)
        .map(it -> m_objectFactory.newInstance(it))
        .map(it -> (IExecutorServiceFactory) it)
        .ifPresent(this::setExecutorServiceFactory);

    if (cla.outputDirectory != null) {
      setOutputDirectory(cla.outputDirectory);
    }

    String testClasses = cla.testClass;
    if (null != testClasses) {
      List<Class<?>> classes = new ArrayList<>();
      for (String c : Utils.splitCommaSeparated(testClasses)) {
        classes.add(ClassHelper.fileToClass(c));
      }

      setTestClasses(classes.toArray(new Class[0]));
    }

    if (cla.testNames != null) {
      setTestNames(Utils.splitCommaSeparated(cla.testNames));
      setIgnoreMissedTestNames(cla.ignoreMissedTestNames);
    }

    // Note: the field is a String, not a Boolean, because the option takes a value, as in
    // "-usedefaultlisteners false".
    if (cla.useDefaultListeners != null) {
      setUseDefaultListeners("true".equalsIgnoreCase(cla.useDefaultListeners));
    }

    setGroups(cla.groups);
    setExcludedGroups(cla.excludedGroups);
    setTestJar(cla.testJar);
    setXmlPathInJar(cla.xmlPathInJar);
    setSkipFailedInvocationCounts(cla.skipFailedInvocationCounts);
    toggleFailureIfAllTestsWereSkipped(cla.failIfAllTestsSkipped);
    setListenersToSkipFromBeingWiredInViaServiceLoaders(
        Utils.splitCommaSeparated(cla.spiListenersToSkip).toArray(new String[0]));

    m_configuration.setOverrideIncludedMethods(cla.overrideIncludedMethods);

    if (cla.parallelMode != null) {
      setParallel(cla.parallelMode);
    }
    if (cla.configFailurePolicy != null) {
      setConfigFailurePolicy(XmlSuite.FailurePolicy.getValidPolicy(cla.configFailurePolicy));
    }
    if (cla.threadCount != null) {
      setThreadCount(cla.threadCount);
    }
    if (cla.dataProviderThreadCount != null) {
      setDataProviderThreadCount(cla.dataProviderThreadCount);
    }
    if (cla.suiteName != null) {
      setDefaultSuiteName(cla.suiteName);
    }
    if (cla.testName != null) {
      setDefaultTestName(cla.testName);
    }
    if (cla.listener != null) {
      String sep = ";";
      if (cla.listener.contains(",")) {
        sep = ",";
      }
      String[] strs = Utils.split(cla.listener, sep);
      List<Class<? extends ITestNGListener>> classes = new ArrayList<>();

      for (String cls : strs) {
        Class<?> clazz = ClassHelper.fileToClass(cls);
        if (ITestNGListener.class.isAssignableFrom(clazz)) {
          classes.add((Class<? extends ITestNGListener>) clazz);
        }
      }

      setListenerClasses(classes);
    }

    if (null != cla.methodSelectors) {
      String[] strs = Utils.split(cla.methodSelectors, ",");
      for (String cls : strs) {
        String[] sel = Utils.split(cls, ":");
        try {
          if (sel.length == 2) {
            addMethodSelector(sel[0], Integer.parseInt(sel[1]));
          } else {
            error("Method selector value was not in the format org.example.Selector:4");
          }
        } catch (NumberFormatException nfe) {
          error("Method selector value was not in the format org.example.Selector:4");
        }
      }
    }

    if (cla.objectFactory != null) {
      setObjectFactory(
          (Class<? extends ITestObjectFactory>) ClassHelper.fileToClass(cla.objectFactory));
    }
    if (cla.testRunnerFactory != null) {
      setTestRunnerFactoryClass(
          (Class<? extends ITestRunnerFactory>) ClassHelper.fileToClass(cla.testRunnerFactory));
    }

    ReporterConfig reporterConfig = ReporterConfig.deserialize(cla.reporter);
    if (reporterConfig != null) {
      addReporter(reporterConfig);
    }

    if (!cla.commandLineMethods.isEmpty()) {
      m_commandLineMethods = cla.commandLineMethods;
    }

    if (cla.suiteFiles != null) {
      setTestSuites(cla.suiteFiles);
    }

    setSuiteThreadPoolSize(cla.suiteThreadPoolSize);
    setRandomizeSuites(cla.randomizeSuites);
    alwaysRunListeners(cla.alwaysRunListeners);
  }

  /**
   * Sets whether TestNG ignores a test name that matches no test, and runs the tests that match.
   *
   * @param ignoreMissedTestNames whether to ignore the names that match no test.
   */
  public void setIgnoreMissedTestNames(boolean ignoreMissedTestNames) {
    m_ignoreMissedTestNames = ignoreMissedTestNames;
  }

  /**
   * Limits the run to the given methods.
   *
   * <p>A {@code null} or empty list removes the limit. This method stores an empty list as {@code
   * null}. An empty list would build a suite with no class, and {@link #setTestClasses(Class[])}
   * would then have no effect.
   *
   * @param methods full method names, as the {@code -methods} command line option takes them.
   */
  public void setCommandLineMethods(List<String> methods) {
    m_commandLineMethods = methods == null || methods.isEmpty() ? null : new ArrayList<>(methods);
  }

  /**
   * Sets whether the command line methods replace the methods that the suite files include.
   *
   * @param overrideIncludedMethods whether to replace the included methods.
   */
  public void setOverrideIncludedMethods(boolean overrideIncludedMethods) {
    m_configuration.setOverrideIncludedMethods(overrideIncludedMethods);
  }

  /**
   * Reports a failure that {@link #run()} itself threw, and records it.
   *
   * <p>The record replaces the result of the run with a single failure. After this call:
   *
   * <ul>
   *   <li>{@link #hasFailure()} returns {@code true}.
   *   <li>{@link #hasSkip()} and {@link #hasFailureWithinSuccessPercentage()} return {@code false}.
   *   <li>{@link #getStatus()} returns 1, but only when the run has test results. Without them, it
   *       returns 8, and the failure does not show (GITHUB-3566).
   * </ul>
   *
   * <p>At a verbose level above 1, this method prints the stack trace to standard output.
   * Otherwise, it logs the message. The logged message appears only when an SLF4J provider is on
   * the class path.
   *
   * <p>A front end that wants an exit status, not an exception, catches the failure and calls this
   * method. This method is here, and not in the caller, because the verbose level of the run
   * decides what it prints.
   *
   * @param cause the failure that the run threw.
   */
  public void reportRunFailure(TestNGException cause) {
    if (TestRunner.getVerbose() > 1) {
      cause.printStackTrace(System.out);
    } else {
      error(cause.getMessage());
    }
    this.exitCode = ExitCode.newExitCodeRepresentingFailure();
  }

  /**
   * Sets the number of suites that run at the same time.
   *
   * @param suiteThreadPoolSize the number of suites.
   */
  public void setSuiteThreadPoolSize(Integer suiteThreadPoolSize) {
    m_suiteThreadPoolSize = suiteThreadPoolSize;
  }

  /**
   * Returns the number of suites that run at the same time.
   *
   * @return the number of suites.
   */
  public Integer getSuiteThreadPoolSize() {
    return m_suiteThreadPoolSize;
  }

  /**
   * Sets whether TestNG runs the suites in random order, instead of the order in the XML.
   *
   * @param randomizeSuites whether to use random order.
   */
  public void setRandomizeSuites(boolean randomizeSuites) {
    m_randomizeSuites = randomizeSuites;
  }

  /**
   * Sets whether TestNG runs the {@link IInvokedMethodListener} listeners for skipped methods too.
   *
   * @param alwaysRun whether to run the listeners for skipped methods.
   */
  public void alwaysRunListeners(boolean alwaysRun) {
    m_alwaysRun = alwaysRun;
  }

  /**
   * Does nothing. Maven Surefire calls this method, so keep it until Surefire stops calling it.
   *
   * @param path not used.
   * @deprecated This method does nothing.
   */
  @Deprecated
  public void setSourcePath(String path) {
    // Nothing to do
  }

  private static int parseInt(@Nullable Object value) {
    if (value == null) {
      return -1;
    }
    if (value instanceof String) {
      return Integer.parseInt(String.valueOf(value));
    }
    if (value instanceof Integer) {
      return (Integer) value;
    }
    throw new IllegalArgumentException("Unable to parse " + value + " as an Integer.");
  }

  /**
   * Sets up this object from a map of command line values.
   *
   * <p>Maven Surefire calls this method. Do not remove it unless you know that Surefire no longer
   * calls it.
   *
   * @param cmdLineArgs the values, keyed by option name, such as {@code -groups}.
   * @deprecated The command line code is in the {@code testng-cli} module. Use {@code
   *     org.testng.cli.CliConfigurer#configure(TestNG, org.testng.cli.CliOptions)}.
   */
  @SuppressWarnings({"unchecked"})
  @Deprecated
  public void configure(Map cmdLineArgs) {
    CommandLineArgs result = new CommandLineArgs();

    int value = parseInt(cmdLineArgs.get(CommandLineArgs.LOG));
    if (value != -1) {
      result.verbose = value;
    }
    result.outputDirectory = (String) cmdLineArgs.get(CommandLineArgs.OUTPUT_DIRECTORY);

    String testClasses = (String) cmdLineArgs.get(CommandLineArgs.TEST_CLASS);
    if (null != testClasses) {
      result.testClass = testClasses;
    }

    String testNames = (String) cmdLineArgs.get(CommandLineArgs.TEST_NAMES);
    if (testNames != null) {
      result.testNames = testNames;
    }

    String useDefaultListeners = (String) cmdLineArgs.get(CommandLineArgs.USE_DEFAULT_LISTENERS);
    if (null != useDefaultListeners) {
      result.useDefaultListeners = useDefaultListeners;
    }

    result.groups = (String) cmdLineArgs.get(CommandLineArgs.GROUPS);
    result.excludedGroups = (String) cmdLineArgs.get(CommandLineArgs.EXCLUDED_GROUPS);
    result.testJar = (String) cmdLineArgs.get(CommandLineArgs.TEST_JAR);
    result.xmlPathInJar =
        (String)
            cmdLineArgs.getOrDefault(
                CommandLineArgs.XML_PATH_IN_JAR, CommandLineArgs.XML_PATH_IN_JAR_DEFAULT);
    result.mixed = (Boolean) cmdLineArgs.getOrDefault(CommandLineArgs.MIXED, false);
    Object tmpValue = cmdLineArgs.get(CommandLineArgs.INCLUDE_ALL_DATA_DRIVEN_TESTS_WHEN_SKIPPING);
    if (tmpValue != null) {
      result.includeAllDataDrivenTestsWhenSkipping = Boolean.parseBoolean(tmpValue.toString());
    }
    Object emptyDpBehavior = cmdLineArgs.get(CommandLineArgs.EMPTY_DATA_PROVIDER_BEHAVIOR);
    if (emptyDpBehavior != null) {
      result.emptyDataProviderBehavior =
          emptyDpBehavior instanceof EmptyDataProviderBehavior
              ? (EmptyDataProviderBehavior) emptyDpBehavior
              : EmptyDataProviderBehavior.valueOf(
                  emptyDpBehavior.toString().toUpperCase(Locale.ROOT));
    }
    result.skipFailedInvocationCounts =
        (Boolean) cmdLineArgs.get(CommandLineArgs.SKIP_FAILED_INVOCATION_COUNTS);
    result.failIfAllTestsSkipped =
        Boolean.parseBoolean(
            cmdLineArgs.getOrDefault(CommandLineArgs.FAIL_IF_ALL_TESTS_SKIPPED, false).toString());
    result.spiListenersToSkip =
        (String) cmdLineArgs.getOrDefault(CommandLineArgs.LISTENERS_TO_SKIP_VIA_SPI, "");
    String parallelMode = (String) cmdLineArgs.get(CommandLineArgs.PARALLEL);
    if (parallelMode != null) {
      result.parallelMode = XmlSuite.ParallelMode.getValidParallel(parallelMode);
    }

    value = parseInt(cmdLineArgs.get(CommandLineArgs.THREAD_COUNT));
    if (value != -1) {
      result.threadCount = value;
    }

    // Surefire does not pass this value yet
    value = parseInt(cmdLineArgs.get(CommandLineArgs.DATA_PROVIDER_THREAD_COUNT));
    if (value != -1) {
      result.dataProviderThreadCount = value;
    }
    String defaultSuiteName = (String) cmdLineArgs.get(CommandLineArgs.SUITE_NAME);
    if (defaultSuiteName != null) {
      result.suiteName = defaultSuiteName;
    }

    String defaultTestName = (String) cmdLineArgs.get(CommandLineArgs.TEST_NAME);
    if (defaultTestName != null) {
      result.testName = defaultTestName;
    }

    Object listeners = cmdLineArgs.get(CommandLineArgs.LISTENER);
    if (listeners instanceof List) {
      result.listener = Utils.join((List<?>) listeners, ",");
    } else {
      result.listener = (String) listeners;
    }

    String ms = (String) cmdLineArgs.get(CommandLineArgs.METHOD_SELECTORS);
    if (null != ms) {
      result.methodSelectors = ms;
    }

    String objectFactory = (String) cmdLineArgs.get(CommandLineArgs.OBJECT_FACTORY);
    if (null != objectFactory) {
      result.objectFactory = objectFactory;
    }

    String runnerFactory = (String) cmdLineArgs.get(CommandLineArgs.TEST_RUNNER_FACTORY);
    if (null != runnerFactory) {
      result.testRunnerFactory = runnerFactory;
    }

    String reporterConfigs = (String) cmdLineArgs.get(CommandLineArgs.REPORTER);
    if (reporterConfigs != null) {
      result.reporter = reporterConfigs;
    }

    String failurePolicy = (String) cmdLineArgs.get(CommandLineArgs.CONFIG_FAILURE_POLICY);
    if (failurePolicy != null) {
      result.configFailurePolicy = failurePolicy;
    }

    value = parseInt(cmdLineArgs.get(CommandLineArgs.SUITE_THREAD_POOL_SIZE));
    if (value != -1) {
      result.suiteThreadPoolSize = value;
    }

    String dependencyInjectorFactoryClass =
        (String) cmdLineArgs.get(CommandLineArgs.DEPENDENCY_INJECTOR_FACTORY);
    if (dependencyInjectorFactoryClass != null) {
      result.dependencyInjectorFactoryClass = dependencyInjectorFactoryClass;
    }

    result.ignoreMissedTestNames =
        Boolean.parseBoolean(
            cmdLineArgs.getOrDefault(CommandLineArgs.IGNORE_MISSED_TEST_NAMES, false).toString());

    result.generateResultsPerSuite =
        Boolean.parseBoolean(
            cmdLineArgs.getOrDefault(CommandLineArgs.GENERATE_RESULTS_PER_SUITE, false).toString());

    Optional.ofNullable(cmdLineArgs.get(CommandLineArgs.LISTENER_COMPARATOR))
        .map(Object::toString)
        .ifPresent(it -> result.listenerComparator = it);

    configure(result);
  }

  /**
   * Limits the run to the {@code <test>} tags with these names.
   *
   * @param testNames the names of the tests to run.
   */
  public void setTestNames(List<String> testNames) {
    m_testNames = testNames;
  }

  /**
   * Sets whether TestNG skips the remaining invocations of a test method after one invocation
   * fails.
   *
   * <p>A value that is not {@code null} overrides the setting of every suite. The default value is
   * {@code false}, so by default this object turns the setting off in every suite. Pass {@code
   * null} to keep the setting of each suite.
   *
   * @param skip whether to skip the remaining invocations, or {@code null}.
   */
  public void setSkipFailedInvocationCounts(@Nullable Boolean skip) {
    m_skipFailedInvocationCounts = skip;
  }

  /**
   * Adds a reporter, written in the form of the {@code -reporter} command line option. An example
   * is {@code com.acme.MyReporter:fileName=out.html}.
   *
   * <p>This method loads and creates the class at once:
   *
   * <ul>
   *   <li>It ignores a {@code null} or empty value.
   *   <li>It logs a warning and skips a class that it cannot find.
   *   <li>It throws when the class is not an {@link IReporter}.
   * </ul>
   *
   * @param reporterConfigString the reporter and its settings.
   * @throws TestNGException when the class is not an {@link IReporter}.
   */
  public void addReporter(@Nullable String reporterConfigString) {
    ReporterConfig reporterConfig = ReporterConfig.deserialize(reporterConfigString);
    if (reporterConfig != null) {
      addReporter(reporterConfig);
    }
  }

  private void addReporter(ReporterConfig reporterConfig) {
    IReporter instance = newReporterInstance(reporterConfig);
    if (instance != null) {
      addListener(instance);
    } else {
      LOGGER.warn("Could not find reporter class : " + reporterConfig.getClassName());
    }
  }

  /**
   * Creates a reporter from its settings.
   *
   * @return the reporter, or {@code null} when TestNG cannot find the class.
   * @throws TestNGException when the class is not an {@link IReporter}.
   */
  private @Nullable IReporter newReporterInstance(ReporterConfig config) {

    Class<?> reporterClass = ClassHelper.forName(config.getClassName());
    if (reporterClass == null) {
      return null;
    }
    if (!IReporter.class.isAssignableFrom(reporterClass)) {
      throw new TestNGException(config.getClassName() + " is not a IReporter");
    }

    IReporter reporter = (IReporter) m_objectFactory.newInstance(reporterClass);

    reporter.getConfig().setProperties(config.getProperties());

    return reporter;
  }

  /**
   * Checks that the command line values select something to run.
   *
   * <p>It fails when no suite file, class, method or jar is given. It also fails when groups are
   * given without classes, suite files or a jar.
   *
   * @param args the command line values to check.
   * @throws TestNGException when a check fails. Since 7.13, this method throws {@link
   *     TestNGException}, not the {@code ParameterException} of JCommander.
   * @deprecated since 7.13. Use {@code org.testng.cli.CliConfigurer#validate} from the {@code
   *     testng-cli} module.
   */
  @Deprecated
  protected static void validateCommandLineParameters(CommandLineArgs args) {
    // Check what configure() makes of the value, not the raw text. -testclass "" and
    // -testclass " , ," name no class at all. Reject that here. Otherwise the run has no classes
    // and reports no error.
    String testClasses = args.testClass;
    if (testClasses != null && Utils.splitCommaSeparated(testClasses).isEmpty()) {
      testClasses = null;
    }
    List<String> testNgXml = args.suiteFiles;
    String testJar = args.testJar;
    List<String> methods = args.commandLineMethods;

    if (testClasses == null
        && testJar == null
        && (testNgXml == null || testNgXml.isEmpty())
        && (methods == null || methods.isEmpty())) {
      throw new TestNGException(
          "You need to specify at least one testng.xml, one class" + " or one method");
    }

    String groups = args.groups;
    String excludedGroups = args.excludedGroups;

    if (testJar == null
        && (null != groups || null != excludedGroups)
        && testClasses == null
        && (testNgXml == null || testNgXml.isEmpty())) {
      throw new TestNGException("Groups option should be used with testclass option");
    }
  }

  /** Returns the outcome of the run. It fails when {@link #run()} has not produced one yet. */
  private ExitCode requireExitCode() {
    return Objects.requireNonNull(this.exitCode, "the run has not produced an exit code yet");
  }

  /**
   * Tells if a test or a configuration method failed, or if {@link
   * #reportRunFailure(TestNGException)} recorded a failure. Call it after {@link #run()}.
   *
   * <p>This method does not check whether the run has test results, as {@link #getStatus()} does.
   *
   * @return {@code true} when there was such a failure.
   * @throws NullPointerException when the run has not produced a result yet.
   */
  public boolean hasFailure() {
    return requireExitCode().hasFailure();
  }

  /**
   * Tells if at least one test failed, but stayed within the success percentage of its method. Call
   * it after {@link #run()}.
   *
   * @return {@code true} when at least one such test failed.
   * @throws NullPointerException when the run has not produced a result yet.
   */
  public boolean hasFailureWithinSuccessPercentage() {
    return requireExitCode().hasFailureWithinSuccessPercentage();
  }

  /**
   * Tells if TestNG skipped a test or a configuration method. Call it after {@link #run()}.
   *
   * @return {@code true} when TestNG skipped at least one test or configuration method.
   * @throws NullPointerException when the run has not produced a result yet.
   */
  public boolean hasSkip() {
    return requireExitCode().hasSkip();
  }

  static void exitWithError(@Nullable String msg) {
    // Trim the message. TestNGException puts a newline in front of every message, which would print
    // an empty line before the error.
    System.err.println(msg == null ? "" : msg.trim());
    usage();
    System.exit(1);
  }

  /**
   * Returns the directory where TestNG writes the reports.
   *
   * @return the directory.
   */
  public String getOutputDirectory() {
    return m_outputDir;
  }

  /**
   * Returns the annotation transformer of the run.
   *
   * <p>Without a transformer of your own, it is the default one. The default transformer turns off
   * the tests marked {@code @Ignore}.
   *
   * @return the annotation transformer.
   */
  public IAnnotationTransformer getAnnotationTransformer() {
    return m_annotationTransformer;
  }

  @SuppressWarnings("ReferenceEquality")
  private void setAnnotationTransformer(IAnnotationTransformer t) {
    // Compare with != on purpose. The warning is for a second, different transformer that replaces
    // one the caller already set. Setting the same object again is not a conflict. A user class can
    // declare two different transformers equal, and equals() would then hide a real conflict.
    if (m_annotationTransformer != m_defaultAnnoProcessor && m_annotationTransformer != t) {
      LOGGER.warn("AnnotationTransformer already set");
    }
    m_annotationTransformer = t;
  }

  /**
   * Returns the default suite name, for a suite that TestNG builds from command line options.
   *
   * @return the name.
   */
  public String getDefaultSuiteName() {
    return m_defaultSuiteName;
  }

  /**
   * Sets the default suite name, for a suite that TestNG builds from command line options.
   *
   * @param defaultSuiteName the name.
   */
  public void setDefaultSuiteName(String defaultSuiteName) {
    m_defaultSuiteName = defaultSuiteName;
  }

  /**
   * Returns the default test name, for a test that TestNG builds from command line options.
   *
   * @return the name.
   */
  public String getDefaultTestName() {
    return m_defaultTestName;
  }

  /**
   * Sets the default test name, for a test that TestNG builds from command line options.
   *
   * @param defaultTestName the name.
   */
  public void setDefaultTestName(String defaultTestName) {
    m_defaultTestName = defaultTestName;
  }

  /**
   * Sets what TestNG does after a configuration method fails.
   *
   * <p>See {@link org.testng.xml.XmlSuite.FailurePolicy} for what each value does. The default
   * value is {@link org.testng.xml.XmlSuite.FailurePolicy#SKIP}.
   *
   * @param failurePolicy the policy, or {@code null} to keep the policy of each suite.
   */
  public void setConfigFailurePolicy(XmlSuite.@Nullable FailurePolicy failurePolicy) {
    m_configFailurePolicy = failurePolicy;
  }

  /**
   * Returns what TestNG does after a configuration method fails.
   *
   * @return the policy, or {@code null} when none was set.
   */
  public XmlSuite.@Nullable FailurePolicy getConfigFailurePolicy() {
    return m_configFailurePolicy;
  }

  // Deprecated: remove it in the next major version.
  /**
   * Returns the TestNG object that was created last. After {@link #run()} ends, it returns {@code
   * null}.
   *
   * @return the TestNG object, or {@code null}.
   * @deprecated since 5.1.
   */
  @Deprecated
  public static @Nullable TestNG getDefault() {
    return m_instance;
  }

  @SuppressWarnings("ReferenceEquality")
  private void setConfigurable(IConfigurable c) {
    // Compare with != on purpose, for the reason given in setAnnotationTransformer.
    if (m_configurable != null && m_configurable != c) {
      LOGGER.warn("Configurable already set");
    }
    m_configurable = c;
  }

  @SuppressWarnings("ReferenceEquality")
  private void setHookable(IHookable h) {
    // Compare with != on purpose, for the reason given in setAnnotationTransformer.
    if (m_hookable != null && m_hookable != h) {
      LOGGER.warn("Hookable already set");
    }
    m_hookable = h;
  }

  /**
   * Adds a method interceptor. Despite its name, this method adds to the interceptors. It does not
   * replace them.
   *
   * @param methodInterceptor the method interceptor.
   */
  public void setMethodInterceptor(IMethodInterceptor methodInterceptor) {
    m_methodInterceptors.add(methodInterceptor);
  }

  /**
   * Sets the number of threads that run data providers in parallel. It overrides the value in the
   * suite files.
   *
   * @param count the number of threads.
   */
  public void setDataProviderThreadCount(int count) {
    m_dataProviderThreadCount = count;
  }

  /**
   * Adds a class loader that TestNG searches when it loads a class by name.
   *
   * <p>The class loader applies to every TestNG object in this JVM. A {@code null} loader changes
   * nothing.
   *
   * @param loader the class loader.
   */
  public void addClassLoader(final ClassLoader loader) {
    if (loader != null) {
      ClassHelper.addClassLoader(loader);
    }
  }

  /**
   * Sets {@code preserve-order} for the tests that TestNG builds from command line options.
   *
   * @param b whether the tests keep the order of their classes and methods.
   */
  public void setPreserveOrder(boolean b) {
    m_preserveOrder = b;
  }

  /**
   * Returns the time when the run started.
   *
   * @return the time, in milliseconds.
   */
  protected long getStart() {
    return m_start;
  }

  /**
   * Returns the time when the run ended.
   *
   * @return the time, in milliseconds.
   */
  protected long getEnd() {
    return m_end;
  }

  /**
   * Sets {@code group-by-instances} for the suites that TestNG builds from command line options.
   *
   * @param b whether to run the methods of one instance together.
   */
  public void setGroupByInstances(boolean b) {
    m_groupByInstances = b;
  }

  /////
  // For the tests of the ServiceLoader support
  //

  private @Nullable URLClassLoader m_serviceLoaderClassLoader;
  private final Map<Class<? extends ITestNGListener>, ITestNGListener> serviceLoaderListeners =
      new HashMap<>();

  /**
   * Sets the class loader in which TestNG looks for listeners through {@link ServiceLoader}. Tests
   * use it.
   *
   * @param ucl the class loader.
   */
  public void setServiceLoaderClassLoader(URLClassLoader ucl) {
    m_serviceLoaderClassLoader = ucl;
  }

  /*
   * Records a listener that ServiceLoader found, so that tests can read it.
   */
  private void addServiceLoaderListener(ITestNGListener l) {
    if (!serviceLoaderListeners.containsKey(l.getClass())) {
      serviceLoaderListeners.put(l.getClass(), l);
    }
  }

  /**
   * Returns the listeners that TestNG loaded through {@link ServiceLoader}. Tests use it.
   *
   * @return a copy of the listeners.
   */
  public List<ITestNGListener> getServiceLoaderListeners() {
    return new ArrayList<>(serviceLoaderListeners.values());
  }

  /**
   * Sets the factory that creates the dependency injector.
   *
   * @param factory the injector factory.
   */
  public void setInjectorFactory(IInjectorFactory factory) {
    this.m_configuration.setInjectorFactory(factory);
  }

  /**
   * Sets the factory that creates the dependency injector, by class.
   *
   * <p>TestNG creates the factory with the object factory that it uses at the time of the call.
   *
   * @param factoryClass the class of the factory.
   */
  public void setInjectorFactoryClass(Class<? extends IInjectorFactory> factoryClass) {
    setInjectorFactory(m_objectFactory.newInstance(factoryClass));
  }
}
