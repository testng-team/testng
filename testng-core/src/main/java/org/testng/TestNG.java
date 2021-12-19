package org.testng;

import static org.testng.internal.Utils.defaultIfStringEmpty;
import static org.testng.internal.Utils.isStringEmpty;
import static org.testng.internal.Utils.isStringNotEmpty;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.collections.Sets;
import org.testng.internal.ClassHelper;
import org.testng.internal.Configuration;
import org.testng.internal.DynamicGraph;
import org.testng.internal.ExitCode;
import org.testng.internal.IConfiguration;
import org.testng.internal.OverrideProcessor;
import org.testng.internal.ReporterConfig;
import org.testng.internal.RuntimeBehavior;
import org.testng.internal.Systematiser;
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
import org.testng.internal.thread.graph.SuiteWorkerFactory;
import org.testng.junit.JUnitTestFinder;
import org.testng.log4testng.Logger;
import org.testng.reporters.EmailableReporter;
import org.testng.reporters.EmailableReporter2;
import org.testng.reporters.FailedReporter;
import org.testng.reporters.JUnitReportReporter;
import org.testng.reporters.SuiteHTMLReporter;
import org.testng.reporters.VerboseReporter;
import org.testng.reporters.XMLReporter;
import org.testng.reporters.jq.Main;
import org.testng.thread.IExecutorFactory;
import org.testng.thread.ITestNGThreadPoolExecutor;
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
 * This class is the main entry point for running tests in the TestNG framework. Users can create
 * their own TestNG object and invoke it in many different ways:
 *
 * <ul>
 *   <li>On an existing testng.xml
 *   <li>On a synthetic testng.xml, created entirely from Java
 *   <li>By directly setting the test classes
 * </ul>
 *
 * You can also define which groups to include or exclude, assign parameters, etc...
 *
 * <p>The command line parameters are:
 *
 * <UL>
 *   <LI>-d <code>outputdir</code>: specify the output directory
 *   <LI>-testclass <code>class_name</code>: specifies one or several class names
 *   <LI>-testjar <code>jar_name</code>: specifies the jar containing the tests
 *   <LI>-sourcedir <code>src1;src2</code>: ; separated list of source directories (used only when
 *       javadoc annotations are used)
 *   <LI>-target
 *   <LI>-groups
 *   <LI>-testrunfactory
 *   <LI>-listener
 * </UL>
 *
 * <p>Please consult documentation for more details.
 *
 * <p>FIXME: should support more than simple paths for suite xmls
 *
 * @see #usage()
 * @author <a href = "mailto:cedric&#64;beust.com">Cedric Beust</a>
 */
@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class TestNG {

  /** This class' log4testng Logger. */
  private static final Logger LOGGER = Logger.getLogger(TestNG.class);

  /** The default name for a suite launched from the command line */
  public static final String DEFAULT_COMMAND_LINE_SUITE_NAME = "Command line suite";

  /** The default name for a test launched from the command line */
  public static final String DEFAULT_COMMAND_LINE_TEST_NAME = "Command line test";

  private static final String DEFAULT_THREADPOOL_FACTORY =
      "org.testng.internal.thread.DefaultThreadPoolExecutorFactory";

  /** The default name of the result's output directory (keep public, used by Eclipse). */
  public static final String DEFAULT_OUTPUTDIR = "test-output";

  private static TestNG m_instance;

  private static JCommander m_jCommander;

  private List<String> m_commandLineMethods;
  protected List<XmlSuite> m_suites = Lists.newArrayList();
  private List<XmlSuite> m_cmdlineSuites;
  private String m_outputDir = DEFAULT_OUTPUTDIR;

  private String[] m_includedGroups;
  private String[] m_excludedGroups;

  private Boolean m_isJUnit = XmlSuite.DEFAULT_JUNIT;
  private Boolean m_isMixed = XmlSuite.DEFAULT_MIXED;
  protected boolean m_useDefaultListeners = true;
  private boolean m_failIfAllTestsSkipped = false;
  private final List<String> m_listenersToSkipFromBeingWiredIn = new ArrayList<>();

  private ITestRunnerFactory m_testRunnerFactory;

  // These listeners can be overridden from the command line
  private final Map<Class<? extends IClassListener>, IClassListener> m_classListeners =
      Maps.newLinkedHashMap();
  private final Map<Class<? extends ITestListener>, ITestListener> m_testListeners =
      Maps.newLinkedHashMap();
  private final Map<Class<? extends ISuiteListener>, ISuiteListener> m_suiteListeners =
      Maps.newLinkedHashMap();
  private final Map<Class<? extends IReporter>, IReporter> m_reporters = Maps.newLinkedHashMap();
  private final Map<Class<? extends IDataProviderListener>, IDataProviderListener>
      m_dataProviderListeners = Maps.newLinkedHashMap();
  private final Map<Class<? extends IDataProviderInterceptor>, IDataProviderInterceptor>
      m_dataProviderInterceptors = Maps.newLinkedHashMap();

  private IExecutorFactory m_executorFactory = null;

  public static final Integer DEFAULT_VERBOSE = 1;

  // Command line suite parameters
  private int m_threadCount = -1;
  private XmlSuite.ParallelMode m_parallelMode = null;
  private XmlSuite.FailurePolicy m_configFailurePolicy;
  private Class<?>[] m_commandLineTestClasses;

  private String m_defaultSuiteName = DEFAULT_COMMAND_LINE_SUITE_NAME;
  private String m_defaultTestName = DEFAULT_COMMAND_LINE_TEST_NAME;

  private final Map<String, Integer> m_methodDescriptors = Maps.newHashMap();

  private final Set<XmlMethodSelector> m_selectors = Sets.newLinkedHashSet();

  private static final ITestObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultTestObjectFactory();
  private ITestObjectFactory m_objectFactory = DEFAULT_OBJECT_FACTORY;

  private final Map<Class<? extends IInvokedMethodListener>, IInvokedMethodListener>
      m_invokedMethodListeners = Maps.newLinkedHashMap();

  private Integer m_dataProviderThreadCount = null;

  private String m_jarPath;
  /** The path of the testng.xml file inside the jar file */
  private String m_xmlPathInJar = CommandLineArgs.XML_PATH_IN_JAR_DEFAULT;

  private List<String> m_stringSuites = Lists.newArrayList();

  private IHookable m_hookable;
  private IConfigurable m_configurable;

  protected long m_end;
  protected long m_start;

  private final Map<Class<? extends IAlterSuiteListener>, IAlterSuiteListener>
      m_alterSuiteListeners = Maps.newLinkedHashMap();

  private boolean m_isInitialized = false;
  private boolean isSuiteInitialized = false;
  private final org.testng.internal.ExitCodeListener exitCodeListener =
      new org.testng.internal.ExitCodeListener();
  private ExitCode exitCode;
  private final Map<Class<? extends IExecutionVisualiser>, IExecutionVisualiser>
      m_executionVisualisers = Maps.newLinkedHashMap();

  /** Default constructor. Setting also usage of default listeners/reporters. */
  public TestNG() {
    init(true);
    if (RuntimeBehavior.isMemoryFriendlyMode()) {
      Logger.getLogger(TestNG.class).warn("TestNG is running in memory friendly mode.");
    }
  }

  /**
   * Used by maven2 to have 0 output of any kind come out of testng.
   *
   * @param useDefaultListeners Whether or not any default reports should be added to tests.
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
   * @param failIfAllTestsSkipped - Whether TestNG should enable/disable failing when all the tests
   *     were skipped and nothing was run (Mostly when a test is powered by a data provider and when
   *     the data provider itself fails causing all tests to skip).
   */
  public void toggleFailureIfAllTestsWereSkipped(boolean failIfAllTestsSkipped) {
    this.m_failIfAllTestsSkipped = failIfAllTestsSkipped;
  }

  /**
   * @param listeners - An array of fully qualified class names that should be skipped from being
   *     wired in via service loaders.
   */
  public void setListenersToSkipFromBeingWiredInViaServiceLoaders(String... listeners) {
    m_listenersToSkipFromBeingWiredIn.addAll(Arrays.asList(listeners));
  }

  public int getStatus() {
    if (exitCodeListener.noTestsFound()) {
      return ExitCode.HAS_NO_TEST;
    }
    return exitCode.getExitCode();
  }

  /**
   * Sets the output directory where the reports will be created.
   *
   * @param outputdir The directory.
   */
  public void setOutputDirectory(final String outputdir) {
    if (isStringNotEmpty(outputdir)) {
      m_outputDir = outputdir;
    }
  }

  /**
   * @param useDefaultListeners If true before run(), the default listeners will not be used.
   *     <ul>
   *       <li>org.testng.reporters.TestHTMLReporter
   *       <li>org.testng.reporters.JUnitXMLReporter
   *       <li>org.testng.reporters.XMLReporter
   *     </ul>
   *
   * @see org.testng.reporters.TestHTMLReporter
   * @see org.testng.reporters.JUnitXMLReporter
   * @see org.testng.reporters.XMLReporter
   */
  public void setUseDefaultListeners(boolean useDefaultListeners) {
    m_useDefaultListeners = useDefaultListeners;
  }

  /**
   * Sets a jar containing a testng.xml file.
   *
   * @param jarPath - Path of the jar
   */
  public void setTestJar(String jarPath) {
    m_jarPath = jarPath;
  }

  /** @param xmlPathInJar Sets the path to the XML file in the test jar file. */
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
      e.printStackTrace(System.out);
    } catch (Exception ex) {
      // Probably a Yaml exception, unnest it
      Throwable t = ex;
      while (t.getCause() != null) {
        t = t.getCause();
      }
      if (t instanceof TestNGException) {
        throw (TestNGException) t;
      }
      throw new TestNGException(t);
    }

    return Collections.emptySet();
  }

  private Collection<XmlSuite> processCommandLineArgs(Collection<XmlSuite> allSuites) {
    Collection<XmlSuite> result = new ArrayList<>();
    for (XmlSuite s : allSuites) {
      processParallelModeCommandLineArgs(s);
      if (m_testNames == null) {
        result.add(s);
        continue;
      }
      // If test names were specified, only run these test names
      TestNamesMatcher testNamesMatcher = new TestNamesMatcher(s, m_testNames);
      List<String> missMatchedTestname = testNamesMatcher.getMissMatchedTestNames();
      if (!missMatchedTestname.isEmpty()) {
        throw new TestNGException("The test(s) <" + missMatchedTestname + "> cannot be found.");
      }
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

  public void initializeSuitesAndJarFile() {
    // The IntelliJ plug-in might have invoked this method already so don't initialize suites twice.
    if (isSuiteInitialized) {
      return;
    }
    isSuiteInitialized = true;

    if (!m_suites.isEmpty()) {
      parseSuiteFiles(); // to parse the suite files (<suite-file>), if any
      return;
    }

    //
    // Parse the suites that were passed on the command line
    //
    for (String suitePath : m_stringSuites) {
      Collection<XmlSuite> allSuites = parseSuite(suitePath);
      m_suites.addAll(processCommandLineArgs(allSuites));
    }

    //
    // jar path
    //
    // If suites were passed on the command line, they take precedence over the suite file
    // inside that jar path
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

    // We have a jar file and no XML file was specified: try to find an XML file inside the jar
    File jarFile = new File(m_jarPath);

    JarFileUtils utils =
        new JarFileUtils(getProcessor(), m_xmlPathInJar, m_testNames, m_parallelMode);

    Collection<XmlSuite> allSuites = utils.extractSuitesFrom(jarFile);
    m_suites.addAll(processCommandLineArgs(allSuites));
  }

  /** @param threadCount Define the number of threads in the thread pool. */
  public void setThreadCount(int threadCount) {
    if (threadCount < 1) {
      exitWithError("Cannot use a threadCount parameter less than 1; 1 > " + threadCount);
    }

    m_threadCount = threadCount;
  }

  /**
   * @param parallel Define whether this run will be run in parallel mode.
   * @deprecated Use #setParallel(XmlSuite.ParallelMode) instead
   */
  @Deprecated
  // TODO: krmahadevan: This method is being used by Gradle. Removal causes build failures.
  public void setParallel(String parallel) {
    setParallel(XmlSuite.ParallelMode.getValidParallel(parallel));
  }

  public void setParallel(XmlSuite.ParallelMode parallel) {
    m_parallelMode = parallel;
  }

  public void setCommandLineSuite(XmlSuite suite) {
    m_cmdlineSuites = Lists.newArrayList();
    m_cmdlineSuites.add(suite);
    m_suites.add(suite);
  }

  /**
   * Set the test classes to be run by this TestNG object. This method will create a dummy suite
   * that will wrap these classes called "Command Line Test".
   *
   * <p>If used together with threadCount, parallel, groups, excludedGroups than this one must be
   * set first.
   *
   * @param classes An array of classes that contain TestNG annotations.
   */
  public void setTestClasses(Class[] classes) {
    m_suites.clear();
    m_commandLineTestClasses = classes;
  }

  /**
   * Given a string com.example.Foo.f1, return an array where [0] is the class and [1] is the
   * method.
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
   * @return a list of XmlSuite objects that represent the list of classes and methods passed in
   *     parameter.
   * @param commandLineMethods a string with the form "com.example.Foo.f1,com.example.Bar.f2"
   */
  private List<XmlSuite> createCommandLineSuitesForMethods(List<String> commandLineMethods) {
    //
    // Create the <classes> tag
    //
    Set<Class> classes = Sets.newHashSet();
    for (String m : commandLineMethods) {
      Class c = ClassHelper.forName(splitMethod(m)[0]);
      if (c != null) {
        classes.add(c);
      }
    }

    List<XmlSuite> result = createCommandLineSuitesForClasses(classes.toArray(new Class[0]));

    //
    // Add the method tags
    //
    List<XmlClass> xmlClasses = Lists.newArrayList();
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
    // See if any of the classes has an xmlSuite or xmlTest attribute.
    // If it does, create the appropriate XmlSuite, otherwise, create
    // the default one
    //

    XmlClass[] xmlClasses =
        Arrays.stream(classes).map(clazz -> new XmlClass(clazz, true)).toArray(XmlClass[]::new);
    Map<String, XmlSuite> suites = Maps.newHashMap();
    IAnnotationFinder finder = m_configuration.getAnnotationFinder();

    for (int i = 0; i < classes.length; i++) {
      Class<?> c = classes[i];
      ITestAnnotation test = finder.findAnnotation(c, ITestAnnotation.class);
      String suiteName = getDefaultSuiteName();
      String testName = getDefaultTestName();
      boolean isJUnit = false;
      if (test != null) {
        suiteName = defaultIfStringEmpty(test.getSuiteName(), suiteName);
        testName = defaultIfStringEmpty(test.getTestName(), testName);
      } else {
        if (m_isMixed && JUnitTestFinder.isJUnitTest(c)) {
          isJUnit = true;
          testName = c.getName();
        }
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
        if (xt.getName().equals(testName)) {
          xmlTest = xt;
          break;
        }
      }

      if (xmlTest == null) {
        xmlTest = new XmlTest(xmlSuite);
        xmlTest.setName(testName);
        xmlTest.setJUnit(isJUnit);
      }

      xmlTest.getXmlClasses().add(xmlClasses[i]);
    }

    return new ArrayList<>(suites.values());
  }

  public void addMethodSelector(String className, int priority) {
    if (Strings.isNotNullAndNotEmpty(className)) {
      m_methodDescriptors.put(className, priority);
    }
  }

  public void addMethodSelector(XmlMethodSelector selector) {
    m_selectors.add(selector);
  }

  public void setReportAllDataDrivenTestsAsSkipped(boolean reportAllDataDrivenTestsAsSkipped) {
    this.m_configuration.setReportAllDataDrivenTestsAsSkipped(reportAllDataDrivenTestsAsSkipped);
  }

  public boolean getReportAllDataDrivenTestsAsSkipped() {
    return this.m_configuration.getReportAllDataDrivenTestsAsSkipped();
  }

  /**
   * Set the suites file names to be run by this TestNG object. This method tries to load and parse
   * the specified TestNG suite xml files. If a file is missing, it is ignored.
   *
   * @param suites A list of paths to one more XML files defining the tests. For example:
   *     <pre>
   * TestNG tng = new TestNG();
   * List&lt;String&gt; suites = Lists.newArrayList();
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
   * Specifies the XmlSuite objects to run.
   *
   * @param suites - The list of {@link XmlSuite} objects.
   * @see org.testng.xml.XmlSuite
   */
  public void setXmlSuites(List<XmlSuite> suites) {
    m_suites = suites;
  }

  /**
   * Define which groups will be excluded from this run.
   *
   * @param groups A list of group names separated by a comma.
   */
  public void setExcludedGroups(String groups) {
    m_excludedGroups = Utils.split(groups, ",");
  }

  /**
   * Define which groups will be included from this run.
   *
   * @param groups A list of group names separated by a comma.
   */
  public void setGroups(String groups) {
    m_includedGroups = Utils.split(groups, ",");
  }

  private void setTestRunnerFactoryClass(
      Class<? extends ITestRunnerFactory> testRunnerFactoryClass) {
    setTestRunnerFactory(m_objectFactory.newInstance(testRunnerFactoryClass));
  }

  protected void setTestRunnerFactory(ITestRunnerFactory itrf) {
    m_testRunnerFactory = itrf;
  }

  public void setObjectFactory(Class<? extends ITestObjectFactory> c) {
    setObjectFactory(m_objectFactory.newInstance(c));
  }

  public void setObjectFactory(ITestObjectFactory factory) {
    m_objectFactory = factory;
  }

  /**
   * Define which listeners to user for this run.
   *
   * @param classes A list of classes, which must be either ISuiteListener, ITestListener or
   *     IReporter
   */
  public void setListenerClasses(List<Class<? extends ITestNGListener>> classes) {
    for (Class<? extends ITestNGListener> cls : classes) {
      addListener(m_objectFactory.newInstance(cls));
    }
  }

  /**
   * @param listener The listener to add
   * @deprecated Use addListener(ITestNGListener) instead
   */
  // TODO remove later /!\ Caution: IntelliJ is using it. Check with @akozlova before removing it
  @Deprecated
  public void addListener(Object listener) {
    if (!(listener instanceof ITestNGListener)) {
      exitWithError(
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

  public void addListener(ITestNGListener listener) {
    if (listener == null) {
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
  }

  public Set<IReporter> getReporters() {
    // This will now cause a different behavior for consumers of this method because unlike before
    // they are no longer
    // going to be getting the original set but only a copy of it (since we internally moved from
    // Sets to Maps)
    return Sets.newHashSet(m_reporters.values());
  }

  public List<ITestListener> getTestListeners() {
    return Lists.newArrayList(m_testListeners.values());
  }

  public List<ISuiteListener> getSuiteListeners() {
    return Lists.newArrayList(m_suiteListeners.values());
  }

  /** If m_verbose gets set, it will override the verbose setting in testng.xml */
  private Integer m_verbose = null;

  private final IAnnotationTransformer m_defaultAnnoProcessor = new DefaultAnnotationTransformer();
  private IAnnotationTransformer m_annotationTransformer = m_defaultAnnoProcessor;

  private Boolean m_skipFailedInvocationCounts = false;

  private final List<IMethodInterceptor> m_methodInterceptors = Lists.newArrayList();

  /** The list of test names to run from the given suite */
  private List<String> m_testNames;

  private Integer m_suiteThreadPoolSize = CommandLineArgs.SUITE_THREAD_POOL_SIZE_DEFAULT;

  private boolean m_randomizeSuites = Boolean.FALSE;

  private boolean m_alwaysRun = Boolean.TRUE;

  private Boolean m_preserveOrder = XmlSuite.DEFAULT_PRESERVE_ORDER;
  private Boolean m_groupByInstances;

  private IConfiguration m_configuration;

  /**
   * Sets the level of verbosity. This value will override the value specified in the test suites.
   *
   * @param verbose the verbosity level (0 to 10 where 10 is most detailed) Actually, this is a lie:
   *     you can specify -1 and this will put TestNG in debug mode (no longer slicing off stack
   *     traces and all).
   */
  public void setVerbose(int verbose) {
    m_verbose = verbose;
  }

  public void setExecutorFactoryClass(String clazzName) {
    this.m_executorFactory = createExecutorFactoryInstanceUsing(clazzName);
  }

  private IExecutorFactory createExecutorFactoryInstanceUsing(String clazzName) {
    Class<?> cls = ClassHelper.forName(clazzName);
    Object instance = m_objectFactory.newInstance(cls);
    if (instance instanceof IExecutorFactory) {
      return (IExecutorFactory) instance;
    }
    throw new IllegalArgumentException(
        clazzName + " does not implement " + IExecutorFactory.class.getName());
  }

  public void setExecutorFactory(IExecutorFactory factory) {
    this.m_executorFactory = factory;
  }

  public IExecutorFactory getExecutorFactory() {
    if (this.m_executorFactory == null) {
      this.m_executorFactory = createExecutorFactoryInstanceUsing(DEFAULT_THREADPOOL_FACTORY);
    }
    return this.m_executorFactory;
  }

  private void initializeCommandLineSuites() {
    if (m_commandLineTestClasses != null || m_commandLineMethods != null) {
      if (null != m_commandLineMethods) {
        m_cmdlineSuites = createCommandLineSuitesForMethods(m_commandLineMethods);
      } else {
        m_cmdlineSuites = createCommandLineSuitesForClasses(m_commandLineTestClasses);
      }

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
    // If groups were specified on the command line, they should override groups
    // specified in the XML file
    boolean hasIncludedGroups = null != m_includedGroups && m_includedGroups.length > 0;
    boolean hasExcludedGroups = null != m_excludedGroups && m_excludedGroups.length > 0;
    List<XmlSuite> suites = m_cmdlineSuites != null ? m_cmdlineSuites : m_suites;
    if (hasIncludedGroups || hasExcludedGroups) {
      for (XmlSuite s : suites) {
        initializeCommandLineSuitesGroups(
            s, hasIncludedGroups, m_includedGroups, hasExcludedGroups, m_excludedGroups);
      }
    }
  }

  private static void initializeCommandLineSuitesGroups(
      XmlSuite s,
      boolean hasIncludedGroups,
      String[] m_includedGroups,
      boolean hasExcludedGroups,
      String[] m_excludedGroups) {
    if (hasIncludedGroups) {
      s.setIncludedGroups(Arrays.asList(m_includedGroups));
    }
    if (hasExcludedGroups) {
      s.setExcludedGroups(Arrays.asList(m_excludedGroups));
    }
    for (XmlSuite child : s.getChildSuites()) {
      initializeCommandLineSuitesGroups(
          child, hasIncludedGroups, m_includedGroups, hasExcludedGroups, m_excludedGroups);
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
    addListener(this.exitCodeListener);
    if (m_useDefaultListeners) {
      addReporter(SuiteHTMLReporter.class);
      addReporter(Main.class);
      addReporter(FailedReporter.class);
      addReporter(XMLReporter.class);
      if (RuntimeBehavior.useOldTestNGEmailableReporter()) {
        addReporter(EmailableReporter.class);
      } else if (RuntimeBehavior.useEmailableReporter()) {
        addReporter(EmailableReporter2.class);
      }
      addReporter(JUnitReportReporter.class);
      if (m_verbose != null && m_verbose > 4) {
        addListener(new VerboseReporter("[TestNG] "));
      }
    }
  }

  private void initializeConfiguration() {
    ITestObjectFactory factory = m_objectFactory;
    //
    // Install the listeners found in ServiceLoader (or use the class
    // loader for tests, if specified).
    //
    addServiceLoaderListeners();

    //
    // Install the listeners found in the suites
    //
    for (XmlSuite s : m_suites) {
      addListeners(s);

      //
      // Install the method selectors
      //
      for (XmlMethodSelector methodSelector : s.getMethodSelectors()) {
        addMethodSelector(methodSelector.getClassName(), methodSelector.getPriority());
        addMethodSelector(methodSelector);
      }

      //
      // Find if we have an object factory
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
    m_configuration.setExecutorFactory(getExecutorFactory());
  }

  private void addListeners(XmlSuite s) {
    IObjectDispenser dispenser = Dispenser.newInstance(m_objectFactory);
    GuiceContext context = new GuiceContext(s, this.m_configuration);
    for (String listenerName : s.getListeners()) {
      Class<?> listenerClass = ClassHelper.forName(listenerName);

      // If specified listener does not exist, a TestNGException will be thrown
      if (listenerClass == null) {
        throw new TestNGException(
            "Listener " + listenerName + " was not found in project's classpath");
      }

      BasicAttributes basic = new BasicAttributes(null, listenerClass);
      CreationAttributes attribute = new CreationAttributes(basic, context);
      Object listener = dispenser.dispense(attribute);
      addListener((ITestNGListener) listener);
    }

    // Add the child suite listeners
    List<XmlSuite> childSuites = s.getChildSuites();
    for (XmlSuite c : childSuites) {
      addListeners(c);
    }
  }

  /** Using reflection to remain Java 5 compliant. */
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
   * Before suites are executed, do a sanity check to ensure all required conditions are met. If
   * not, throw an exception to stop test execution
   *
   * @throws TestNGException if the sanity check fails
   */
  private void sanityCheck() {
    XmlSuiteUtils.validateIfSuitesContainDuplicateTests(m_suites);
    XmlSuiteUtils.adjustSuiteNamesToEnsureUniqueness(m_suites);
  }

  /** Invoked by the remote runner. */
  public void initializeEverything() {
    // The Eclipse plug-in (RemoteTestNG) might have invoked this method already
    // so don't initialize suites twice.
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

  /** Run TestNG. */
  public void run() {
    initializeEverything();
    sanityCheck();

    runExecutionListeners(true /* start */);

    runSuiteAlterationListeners();

    m_start = System.currentTimeMillis();
    List<ISuite> suiteRunners = runSuites();

    m_end = System.currentTimeMillis();

    if (null != suiteRunners) {
      generateReports(suiteRunners);
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
    m_jCommander = null;
  }

  /**
   * Run the test suites.
   *
   * <p>This method can be overridden by subclass. <br>
   * For example, DistributedTestNG to run in master/slave mode according to commandline args.
   *
   * @return - List of suites that were run as {@link ISuite} objects.
   * @since 6.9.11 when moving distributed/remote classes out into separate project
   */
  protected List<ISuite> runSuites() {
    return runSuitesLocally();
  }

  private void runSuiteAlterationListeners() {
    for (IAlterSuiteListener l : m_alterSuiteListeners.values()) {
      l.alter(m_suites);
    }
  }

  private void runExecutionListeners(boolean start) {
    List<IExecutionListener> executionListeners = m_configuration.getExecutionListeners();
    if (start) {
      for (IExecutionListener l : executionListeners) {
        l.onExecutionStart();
      }
    } else {
      List<IExecutionListener> executionListenersReversed =
          Lists.newReversedArrayList(executionListeners);
      for (IExecutionListener l : executionListenersReversed) {
        l.onExecutionFinish();
      }
    }
  }

  private static void usage() {
    if (m_jCommander == null) {
      m_jCommander = new JCommander(new CommandLineArgs());
    }
    m_jCommander.usage();
  }

  private void generateReports(List<ISuite> suiteRunners) {
    for (IReporter reporter : m_reporters.values()) {
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
   * This needs to be public for maven2, for now..At least until an alternative mechanism is found.
   *
   * @return The locally run suites
   */
  public List<ISuite> runSuitesLocally() {
    if (m_suites.isEmpty()) {
      error("No test suite found. Nothing to run");
      usage();
      return Collections.emptyList();
    }

    SuiteRunnerMap suiteRunnerMap = new SuiteRunnerMap();

    if (m_suites.get(0).getVerbose() >= 2) {
      Version.displayBanner();
    }

    // First initialize the suite runners to ensure there are no configuration issues.
    // Create a map with XmlSuite as key and corresponding SuiteRunner as value
    for (XmlSuite xmlSuite : m_suites) {
      createSuiteRunners(suiteRunnerMap, xmlSuite);
    }

    //
    // Run suites
    //
    if (m_suiteThreadPoolSize == 1 && !m_randomizeSuites) {
      // Single threaded and not randomized: run the suites in order
      for (XmlSuite xmlSuite : m_suites) {
        runSuitesSequentially(
            xmlSuite, suiteRunnerMap, getVerbose(xmlSuite), getDefaultSuiteName());
      }
      //
      // Generate the suites report
      //
      return Lists.newArrayList(suiteRunnerMap.values());
    }
    // Multithreaded: generate a dynamic graph that stores the suite hierarchy. This is then
    // used to run related suites in specific order. Parent suites are run only
    // once all the child suites have completed execution
    IDynamicGraph<ISuite> suiteGraph = new DynamicGraph<>();
    for (XmlSuite xmlSuite : m_suites) {
      populateSuiteGraph(suiteGraph, suiteRunnerMap, xmlSuite);
    }

    IThreadWorkerFactory<ISuite> factory =
        new SuiteWorkerFactory(
            suiteRunnerMap, 0 /* verbose hasn't been set yet */, getDefaultSuiteName());
    ITestNGThreadPoolExecutor pooledExecutor =
        this.getExecutorFactory()
            .newSuiteExecutor(
                "suites",
                suiteGraph,
                factory,
                m_suiteThreadPoolSize,
                m_suiteThreadPoolSize,
                Integer.MAX_VALUE,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                null);

    Utils.log("TestNG", 2, "Starting executor for all suites");
    // Run all suites in parallel
    pooledExecutor.run();
    try {
      pooledExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      pooledExecutor.shutdownNow();
    } catch (InterruptedException handled) {
      Thread.currentThread().interrupt();
      error("Error waiting for concurrent executors to finish " + handled.getMessage());
    }

    //
    // Generate the suites report
    //
    return Lists.newArrayList(suiteRunnerMap.values());
  }

  private static void error(String s) {
    LOGGER.error(s);
  }

  /**
   * @return the verbose level, checking in order: the verbose level on the suite, the verbose level
   *     on the TestNG object, or 1.
   */
  private int getVerbose(XmlSuite xmlSuite) {
    return xmlSuite.getVerbose() != null
        ? xmlSuite.getVerbose()
        : (m_verbose != null ? m_verbose : RuntimeBehavior.getDefaultVerboseLevel());
  }

  /**
   * Recursively runs suites. Runs the children suites before running the parent suite. This is done
   * so that the results for parent suite can reflect the combined results of the children suites.
   *
   * @param xmlSuite XML Suite to be executed
   * @param suiteRunnerMap Maps {@code XmlSuite}s to respective {@code ISuite}
   * @param verbose verbose level
   * @param defaultSuiteName default suite name
   */
  private void runSuitesSequentially(
      XmlSuite xmlSuite, SuiteRunnerMap suiteRunnerMap, int verbose, String defaultSuiteName) {
    for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
      runSuitesSequentially(childSuite, suiteRunnerMap, verbose, defaultSuiteName);
    }
    SuiteRunnerWorker srw =
        new SuiteRunnerWorker(
            suiteRunnerMap.get(xmlSuite), suiteRunnerMap, verbose, defaultSuiteName);
    srw.run();
  }

  /**
   * Populates the dynamic graph with the reverse hierarchy of suites. Edges are added pointing from
   * child suite runners to parent suite runners, hence making parent suite runners dependent on all
   * the child suite runners
   *
   * @param suiteGraph dynamic graph representing the reverse hierarchy of SuiteRunners
   * @param suiteRunnerMap Map with XMLSuite as key and its respective SuiteRunner as value
   * @param xmlSuite XML Suite
   */
  private void populateSuiteGraph(
      IDynamicGraph<ISuite> suiteGraph /* OUT */,
      SuiteRunnerMap suiteRunnerMap,
      XmlSuite xmlSuite) {
    ISuite parentSuiteRunner = suiteRunnerMap.get(xmlSuite);
    suiteGraph.addNode(parentSuiteRunner);
    if (!xmlSuite.getChildSuites().isEmpty()) {
      for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
        suiteGraph.addEdge(0, parentSuiteRunner, suiteRunnerMap.get(childSuite));
        populateSuiteGraph(suiteGraph, suiteRunnerMap, childSuite);
      }
    }
  }

  /**
   * Creates the {@code SuiteRunner}s and populates the suite runner map with this information
   *
   * @param suiteRunnerMap Map with XMLSuite as key and it's respective SuiteRunner as value. This
   *     is updated as part of this method call
   * @param xmlSuite Xml Suite (and its children) for which {@code SuiteRunner}s are created
   */
  private void createSuiteRunners(SuiteRunnerMap suiteRunnerMap /* OUT */, XmlSuite xmlSuite) {
    if (null != m_isJUnit && !m_isJUnit.equals(XmlSuite.DEFAULT_JUNIT)) {
      xmlSuite.setJUnit(m_isJUnit);
    }

    // If the skip flag was invoked on the command line, it
    // takes precedence
    if (null != m_skipFailedInvocationCounts) {
      xmlSuite.setSkipFailedInvocationCounts(m_skipFailedInvocationCounts);
    }

    // Override the XmlSuite verbose value with the one from TestNG
    if (m_verbose != null) {
      xmlSuite.setVerbose(m_verbose);
    }

    if (null != m_configFailurePolicy) {
      xmlSuite.setConfigFailurePolicy(m_configFailurePolicy);
    }

    if (null != m_dataProviderThreadCount) {
      xmlSuite.setDataProviderThreadCount(m_dataProviderThreadCount);
    }

    Set<XmlMethodSelector> selectors = Sets.newHashSet();
    for (XmlTest t : xmlSuite.getTests()) {
      for (Map.Entry<String, Integer> ms : m_methodDescriptors.entrySet()) {
        XmlMethodSelector xms = new XmlMethodSelector();
        xms.setName(ms.getKey());
        xms.setPriority(ms.getValue());
        selectors.add(xms);
      }
      selectors.addAll(m_selectors);
      t.getMethodSelectors().addAll(Lists.newArrayList(selectors));
    }

    suiteRunnerMap.put(xmlSuite, createSuiteRunner(xmlSuite));

    for (XmlSuite childSuite : xmlSuite.getChildSuites()) {
      createSuiteRunners(suiteRunnerMap, childSuite);
    }
  }

  /** Creates a suite runner and configures its initial state */
  private SuiteRunner createSuiteRunner(XmlSuite xmlSuite) {
    DataProviderHolder holder = new DataProviderHolder();
    holder.addListeners(m_dataProviderListeners.values());
    holder.addInterceptors(m_dataProviderInterceptors.values());
    SuiteRunner result =
        new SuiteRunner(
            getConfiguration(),
            xmlSuite,
            m_outputDir,
            m_testRunnerFactory,
            m_useDefaultListeners,
            m_methodInterceptors,
            m_invokedMethodListeners.values(),
            m_testListeners.values(),
            m_classListeners.values(),
            holder,
            Systematiser.getComparator());

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

  protected IConfiguration getConfiguration() {
    return m_configuration;
  }

  /**
   * The TestNG entry point for command line execution.
   *
   * @param argv the TestNG command line parameters.
   */
  public static void main(String[] argv) {
    TestNG testng = privateMain(argv, null);
    System.exit(testng.getStatus());
  }

  /**
   * <B>Note</B>: this method is not part of the public API and is meant for internal usage only.
   *
   * @param argv The param arguments
   * @param listener The listener
   * @return The TestNG instance
   */
  public static TestNG privateMain(String[] argv, ITestListener listener) {
    TestNG result = new TestNG();

    if (null != listener) {
      result.addListener(listener);
    }

    //
    // Parse the arguments
    //
    try {
      CommandLineArgs cla = new CommandLineArgs();

      m_jCommander = new JCommander(cla);
      m_jCommander.parse(argv);
      validateCommandLineParameters(cla);
      result.configure(cla);
    } catch (ParameterException ex) {
      exitWithError(ex.getMessage());
    }

    //
    // Run
    //
    try {
      result.run();
    } catch (TestNGException ex) {
      if (TestRunner.getVerbose() > 1) {
        ex.printStackTrace(System.out);
      } else {
        error(ex.getMessage());
      }
      result.exitCode = ExitCode.newExitCodeRepresentingFailure();
    }

    return result;
  }

  /**
   * Configure the TestNG instance based on the command line parameters.
   *
   * @param cla The command line parameters
   */
  protected void configure(CommandLineArgs cla) {
    setReportAllDataDrivenTestsAsSkipped(cla.includeAllDataDrivenTestsWhenSkipping);
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
    if (cla.threadPoolFactoryClass != null) {
      setExecutorFactoryClass(cla.threadPoolFactoryClass);
    }
    setOutputDirectory(cla.outputDirectory);

    String testClasses = cla.testClass;
    if (null != testClasses) {
      String[] strClasses = testClasses.split(",");
      List<Class<?>> classes = Lists.newArrayList();
      for (String c : strClasses) {
        classes.add(ClassHelper.fileToClass(c));
      }

      setTestClasses(classes.toArray(new Class[0]));
    }

    setOutputDirectory(cla.outputDirectory);

    if (cla.testNames != null) {
      setTestNames(Arrays.asList(cla.testNames.split(",")));
    }

    // Note: can't use a Boolean field here because we are allowing a boolean
    // parameter with an arity of 1 ("-usedefaultlisteners false")
    if (cla.useDefaultListeners != null) {
      setUseDefaultListeners("true".equalsIgnoreCase(cla.useDefaultListeners));
    }

    setGroups(cla.groups);
    setExcludedGroups(cla.excludedGroups);
    setTestJar(cla.testJar);
    setXmlPathInJar(cla.xmlPathInJar);
    setJUnit(cla.junit);
    setMixed(cla.mixed);
    setSkipFailedInvocationCounts(cla.skipFailedInvocationCounts);
    toggleFailureIfAllTestsWereSkipped(cla.failIfAllTestsSkipped);
    setListenersToSkipFromBeingWiredInViaServiceLoaders(cla.spiListenersToSkip.split(","));

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
      List<Class<? extends ITestNGListener>> classes = Lists.newArrayList();

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

    if (cla.commandLineMethods.size() > 0) {
      m_commandLineMethods = cla.commandLineMethods;
    }

    if (cla.suiteFiles != null) {
      setTestSuites(cla.suiteFiles);
    }

    setSuiteThreadPoolSize(cla.suiteThreadPoolSize);
    setRandomizeSuites(cla.randomizeSuites);
    alwaysRunListeners(cla.alwaysRunListeners);
  }

  public void setSuiteThreadPoolSize(Integer suiteThreadPoolSize) {
    m_suiteThreadPoolSize = suiteThreadPoolSize;
  }

  public Integer getSuiteThreadPoolSize() {
    return m_suiteThreadPoolSize;
  }

  public void setRandomizeSuites(boolean randomizeSuites) {
    m_randomizeSuites = randomizeSuites;
  }

  public void alwaysRunListeners(boolean alwaysRun) {
    m_alwaysRun = alwaysRun;
  }

  /**
   * This method is invoked by Maven's Surefire, only remove it once Surefire has been modified to
   * no longer call it.
   *
   * @param path The path
   * @deprecated
   */
  @Deprecated
  public void setSourcePath(String path) {
    // nop
  }

  private static int parseInt(Object value) {
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
   * This method is invoked by Maven's Surefire to configure the runner, do not remove unless you
   * know for sure that Surefire has been updated to use the new configure(CommandLineArgs) method.
   *
   * @param cmdLineArgs The command line
   * @deprecated use new configure(CommandLineArgs) method
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
    result.xmlPathInJar = (String) cmdLineArgs.get(CommandLineArgs.XML_PATH_IN_JAR);
    result.junit = (Boolean) cmdLineArgs.get(CommandLineArgs.JUNIT);
    result.mixed = (Boolean) cmdLineArgs.get(CommandLineArgs.MIXED);
    Object tmpValue = cmdLineArgs.get(CommandLineArgs.INCLUDE_ALL_DATA_DRIVEN_TESTS_WHEN_SKIPPING);
    if (tmpValue != null) {
      result.includeAllDataDrivenTestsWhenSkipping = Boolean.parseBoolean(tmpValue.toString());
    }
    result.skipFailedInvocationCounts =
        (Boolean) cmdLineArgs.get(CommandLineArgs.SKIP_FAILED_INVOCATION_COUNTS);
    result.failIfAllTestsSkipped =
        Boolean.parseBoolean(
            cmdLineArgs
                .getOrDefault(CommandLineArgs.FAIL_IF_ALL_TESTS_SKIPPED, Boolean.FALSE)
                .toString());
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

    // Not supported by Surefire yet
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

    configure(result);
  }

  /** @param testNames Only run the specified tests from the suite. */
  public void setTestNames(List<String> testNames) {
    m_testNames = testNames;
  }

  public void setSkipFailedInvocationCounts(Boolean skip) {
    m_skipFailedInvocationCounts = skip;
  }

  private void addReporter(ReporterConfig reporterConfig) {
    IReporter instance = newReporterInstance(reporterConfig);
    if (instance != null) {
      addListener(instance);
    } else {
      LOGGER.warn("Could not find reporter class : " + reporterConfig.getClassName());
    }
  }

  /** Creates a reporter based on the configuration */
  private IReporter newReporterInstance(ReporterConfig config) {

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
   * Specify if this run should be made in JUnit mode
   *
   * @param isJUnit - Specify if this run should be made in JUnit mode
   */
  public void setJUnit(Boolean isJUnit) {
    m_isJUnit = isJUnit;
  }

  /** @param isMixed Specify if this run should be made in mixed mode */
  public void setMixed(Boolean isMixed) {
    if (isMixed == null) {
      return;
    }
    m_isMixed = isMixed;
  }

  /**
   * Double check that the command line parameters are valid.
   *
   * @param args The command line to check
   */
  protected static void validateCommandLineParameters(CommandLineArgs args) {
    String testClasses = args.testClass;
    List<String> testNgXml = args.suiteFiles;
    String testJar = args.testJar;
    List<String> methods = args.commandLineMethods;

    if (testClasses == null
        && testJar == null
        && (testNgXml == null || testNgXml.isEmpty())
        && (methods == null || methods.isEmpty())) {
      throw new ParameterException(
          "You need to specify at least one testng.xml, one class" + " or one method");
    }

    String groups = args.groups;
    String excludedGroups = args.excludedGroups;

    if (testJar == null
        && (null != groups || null != excludedGroups)
        && testClasses == null
        && (testNgXml == null || testNgXml.isEmpty())) {
      throw new ParameterException("Groups option should be used with testclass option");
    }

    Boolean junit = args.junit;
    Boolean mixed = args.mixed;
    if (junit && mixed) {
      throw new ParameterException(
          CommandLineArgs.MIXED + " can't be combined with " + CommandLineArgs.JUNIT);
    }
  }

  /** @return true if at least one test failed. */
  public boolean hasFailure() {
    return this.exitCode.hasFailure();
  }

  /** @return true if at least one test failed within success percentage. */
  public boolean hasFailureWithinSuccessPercentage() {
    return this.exitCode.hasFailureWithinSuccessPercentage();
  }

  /** @return true if at least one test was skipped. */
  public boolean hasSkip() {
    return this.exitCode.hasSkip();
  }

  static void exitWithError(String msg) {
    System.err.println(msg);
    usage();
    System.exit(1);
  }

  public String getOutputDirectory() {
    return m_outputDir;
  }

  public IAnnotationTransformer getAnnotationTransformer() {
    return m_annotationTransformer;
  }

  private void setAnnotationTransformer(IAnnotationTransformer t) {
    // compare by reference!
    if (m_annotationTransformer != m_defaultAnnoProcessor && m_annotationTransformer != t) {
      LOGGER.warn("AnnotationTransformer already set");
    }
    m_annotationTransformer = t;
  }

  /** @return the defaultSuiteName */
  public String getDefaultSuiteName() {
    return m_defaultSuiteName;
  }

  /** @param defaultSuiteName the defaultSuiteName to set */
  public void setDefaultSuiteName(String defaultSuiteName) {
    m_defaultSuiteName = defaultSuiteName;
  }

  /** @return the defaultTestName */
  public String getDefaultTestName() {
    return m_defaultTestName;
  }

  /** @param defaultTestName the defaultTestName to set */
  public void setDefaultTestName(String defaultTestName) {
    m_defaultTestName = defaultTestName;
  }

  /**
   * Sets the policy for whether or not to ever invoke a configuration method again after it has
   * failed once. Possible values are defined in {@link XmlSuite}. The default value is {@link
   * org.testng.xml.XmlSuite.FailurePolicy#SKIP}
   *
   * @param failurePolicy the configuration failure policy
   */
  public void setConfigFailurePolicy(XmlSuite.FailurePolicy failurePolicy) {
    m_configFailurePolicy = failurePolicy;
  }

  /**
   * Returns the configuration failure policy.
   *
   * @return config failure policy
   */
  public XmlSuite.FailurePolicy getConfigFailurePolicy() {
    return m_configFailurePolicy;
  }

  // DEPRECATED: to be removed after a major version change
  /**
   * @return The default instance
   * @deprecated since 5.1
   */
  @Deprecated
  public static TestNG getDefault() {
    return m_instance;
  }

  private void setConfigurable(IConfigurable c) {
    // compare by reference!
    if (m_configurable != null && m_configurable != c) {
      LOGGER.warn("Configurable already set");
    }
    m_configurable = c;
  }

  private void setHookable(IHookable h) {
    // compare by reference!
    if (m_hookable != null && m_hookable != h) {
      LOGGER.warn("Hookable already set");
    }
    m_hookable = h;
  }

  public void setMethodInterceptor(IMethodInterceptor methodInterceptor) {
    m_methodInterceptors.add(methodInterceptor);
  }

  public void setDataProviderThreadCount(int count) {
    m_dataProviderThreadCount = count;
  }

  /**
   * Add a class loader to the searchable loaders.
   *
   * @param loader The class loader to add
   */
  public void addClassLoader(final ClassLoader loader) {
    if (loader != null) {
      ClassHelper.addClassLoader(loader);
    }
  }

  public void setPreserveOrder(boolean b) {
    m_preserveOrder = b;
  }

  protected long getStart() {
    return m_start;
  }

  protected long getEnd() {
    return m_end;
  }

  public void setGroupByInstances(boolean b) {
    m_groupByInstances = b;
  }

  /////
  // ServiceLoader testing
  //

  private URLClassLoader m_serviceLoaderClassLoader;
  private final Map<Class<? extends ITestNGListener>, ITestNGListener> serviceLoaderListeners =
      Maps.newHashMap();

  /*
   * Used to test ServiceClassLoader
   */
  public void setServiceLoaderClassLoader(URLClassLoader ucl) {
    m_serviceLoaderClassLoader = ucl;
  }

  /*
   * Used to test ServiceClassLoader
   */
  private void addServiceLoaderListener(ITestNGListener l) {
    if (!serviceLoaderListeners.containsKey(l.getClass())) {
      serviceLoaderListeners.put(l.getClass(), l);
    }
  }

  /*
   * Used to test ServiceClassLoader
   */
  public List<ITestNGListener> getServiceLoaderListeners() {
    return Lists.newArrayList(serviceLoaderListeners.values());
  }

  public void setInjectorFactory(IInjectorFactory factory) {
    this.m_configuration.setInjectorFactory(factory);
  }
}
