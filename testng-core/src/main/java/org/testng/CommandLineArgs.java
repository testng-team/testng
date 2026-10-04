package org.testng;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.xml.XmlSuite;

/**
 * The values of the TestNG command line options.
 *
 * <p>Since 7.13, the {@code testng-cli} and {@code testng-jcommander} modules hold the command line
 * code. This keeps a command line parsing library out of {@code testng-core}. This class stays,
 * without its JCommander annotations, so that these still work:
 *
 * <ul>
 *   <li>{@link TestNG#configure(CommandLineArgs)}.
 *   <li>{@link TestNG#configure(java.util.Map)}, which fills a {@code CommandLineArgs} from a map.
 *   <li>Subclasses of {@link TestNG}, such as {@code RemoteTestNG}.
 * </ul>
 *
 * <p>The TestNG command line does not fill this class.
 *
 * <p>The constants that end in {@code _DEFAULT} hold default values. Every other constant is the
 * name of a command line option, such as {@code -verbose}. {@link TestNG#configure(java.util.Map)}
 * also uses some of these names as map keys.
 *
 * @deprecated since 7.13. Use {@code org.testng.cli.CliOptions} from the {@code testng-cli} module.
 *     TestNG 8.0 will remove this class.
 */
@Deprecated
public class CommandLineArgs {

  /** The XML suite files to run. */
  public List<String> suiteFiles = new ArrayList<>();

  public static final String LOG = "-log";
  public static final String VERBOSE = "-verbose";

  /** How much detail TestNG prints. With a higher number, TestNG prints more. */
  public @Nullable Integer verbose;

  public static final String GROUPS = "-groups";

  /** The names of the groups to run, separated by commas. */
  public @Nullable String groups;

  public static final String EXCLUDED_GROUPS = "-excludegroups";

  /** The names of the groups to leave out, separated by commas. */
  public @Nullable String excludedGroups;

  public static final String OUTPUT_DIRECTORY = "-d";

  /** The directory where TestNG writes its reports. */
  public @Nullable String outputDirectory;

  public static final String MIXED = "-mixed";

  /**
   * Does nothing. TestNG 7.10.0 removed its support for running JUnit tests. The field stays so
   * that code that sets it keeps working.
   */
  public Boolean mixed = Boolean.FALSE;

  public static final String LISTENER = "-listener";

  /**
   * The listeners to add, as class names or {@code .class} files. Separate them with commas or
   * semicolons. TestNG skips a class that is not an {@link ITestNGListener}.
   */
  public @Nullable String listener;

  public static final String LISTENER_COMPARATOR = "-listenercomparator";

  /**
   * The class name of a {@link ListenerComparator}. It sets the order in which TestNG runs the
   * listeners.
   */
  public @Nullable String listenerComparator;

  public static final String METHOD_SELECTORS = "-methodselectors";

  /**
   * The method selectors to add, separated by commas. Each one has the form {@code
   * className:priority}, for example {@code org.example.Selector:4}. Each class implements {@link
   * IMethodSelector}.
   */
  public @Nullable String methodSelectors;

  public static final String OBJECT_FACTORY = "-objectfactory";

  /**
   * The full class name of the {@link ITestObjectFactory} that creates the instances of the test
   * classes.
   */
  public @Nullable String objectFactory;

  public static final String PARALLEL = "-parallel";

  /** How TestNG runs the tests in parallel. See {@link XmlSuite.ParallelMode}. */
  public XmlSuite.@Nullable ParallelMode parallelMode;

  public static final String CONFIG_FAILURE_POLICY = "-configfailurepolicy";

  /** What TestNG does after a configuration method fails: {@code skip} or {@code continue}. */
  public @Nullable String configFailurePolicy;

  public static final String THREAD_COUNT = "-threadcount";

  /** The number of threads that run tests in parallel. */
  public @Nullable Integer threadCount;

  public static final String DATA_PROVIDER_THREAD_COUNT = "-dataproviderthreadcount";

  /** The number of threads that run data providers in parallel. */
  public @Nullable Integer dataProviderThreadCount;

  public static final String SUITE_NAME = "-suitename";

  /** The suite name to use when neither the suite file nor the code gives one. */
  public @Nullable String suiteName;

  public static final String TEST_NAME = "-testname";

  /** The test name to use when neither the suite file nor the code gives one. */
  public @Nullable String testName;

  public static final String REPORTER = "-reporter";

  /** A custom reporter and its settings, in the form {@code className:name=value,name=value}. */
  public @Nullable String reporter;

  public static final String USE_DEFAULT_LISTENERS = "-usedefaultlisteners";

  /**
   * Whether TestNG adds its default listeners. The value {@code "true"} means yes, and any other
   * value means no. The letter case does not matter.
   */
  public String useDefaultListeners = "true";

  public static final String SKIP_FAILED_INVOCATION_COUNTS = "-skipfailedinvocationcounts";

  /** Whether TestNG skips the remaining invocations of a test method after one invocation fails. */
  public @Nullable Boolean skipFailedInvocationCounts;

  public static final String TEST_CLASS = "-testclass";

  /** The test classes to run, as class names or {@code .class} files, separated by commas. */
  public @Nullable String testClass;

  public static final String TEST_NAMES = "-testnames";

  /** The names of the {@code <test>} tags to run, separated by commas. */
  public @Nullable String testNames;

  public static final String IGNORE_MISSED_TEST_NAMES = "-ignoreMissedTestNames";

  /**
   * Whether TestNG ignores a name in {@code -testnames} that matches no test, and runs the tests
   * that match.
   */
  public boolean ignoreMissedTestNames = false;

  public static final String TEST_JAR = "-testjar";

  /** A jar file containing the tests. */
  public @Nullable String testJar;

  public static final String XML_PATH_IN_JAR = "-xmlpathinjar";
  public static final String XML_PATH_IN_JAR_DEFAULT = "testng.xml";

  /** The path of the suite file inside the jar. TestNG uses it only with {@code -testjar}. */
  public String xmlPathInJar = XML_PATH_IN_JAR_DEFAULT;

  public static final String TEST_RUNNER_FACTORY = "-testrunfactory";

  /**
   * The class name of the {@link ITestRunnerFactory} that creates a {@link TestRunner} for each
   * {@code <test>}.
   */
  public @Nullable String testRunnerFactory;

  public static final String LISTENER_FACTORY = "-listenerfactory";

  /** The class name of the {@link ITestNGListenerFactory} that creates TestNG listeners. */
  public @Nullable String listenerFactory;

  public static final String METHODS = "-methods";

  /**
   * The test methods to run. Each one is a full method name, such as {@code
   * com.example.MyTest.myMethod}.
   */
  public List<String> commandLineMethods = new ArrayList<>();

  public static final String SUITE_THREAD_POOL_SIZE = "-suitethreadpoolsize";
  public static final Integer SUITE_THREAD_POOL_SIZE_DEFAULT = 1;

  /** The number of threads that run suites in parallel. */
  public Integer suiteThreadPoolSize = SUITE_THREAD_POOL_SIZE_DEFAULT;

  public static final String RANDOMIZE_SUITES = "-randomizesuites";

  /** Whether TestNG runs the suites in random order, instead of the order in the XML. */
  public Boolean randomizeSuites = Boolean.FALSE;

  public static final String ALWAYS_RUN_LISTENERS = "-alwaysrunlisteners";

  /** Whether TestNG runs the {@link IInvokedMethodListener} listeners for skipped methods too. */
  public Boolean alwaysRunListeners = Boolean.TRUE;

  public static final String THREAD_POOL_FACTORY_CLASS = "-threadpoolfactoryclass";

  /**
   * The class name of the {@link IExecutorServiceFactory} that creates the thread pools of TestNG.
   */
  public @Nullable String threadPoolFactoryClass;

  public static final String DEPENDENCY_INJECTOR_FACTORY = "-dependencyinjectorfactory";

  /** The class name of the {@link IInjectorFactory} that creates the dependency injector. */
  public @Nullable String dependencyInjectorFactoryClass;

  public static final String FAIL_IF_ALL_TESTS_SKIPPED = "-failwheneverythingskipped";

  /** Whether TestNG reports the run as failed when it skips every test and runs nothing. */
  public Boolean failIfAllTestsSkipped = false;

  public static final String LISTENERS_TO_SKIP_VIA_SPI = "-spilistenerstoskip";

  /**
   * The full class names of the listeners that TestNG must not load through {@link
   * java.util.ServiceLoader}, separated by commas.
   */
  public String spiListenersToSkip = "";

  public static final String OVERRIDE_INCLUDED_METHODS = "-overrideincludedmethods";

  /**
   * Whether the methods given on the command line replace the methods that the suite XML includes.
   */
  public Boolean overrideIncludedMethods = false;

  public static final String INCLUDE_ALL_DATA_DRIVEN_TESTS_WHEN_SKIPPING =
      "-includeAllDataDrivenTestsWhenSkipping";

  public static final String EMPTY_DATA_PROVIDER_BEHAVIOR = "-emptydataproviderbehavior";

  /**
   * Whether TestNG reports each data provider row of a test method as its own skip. This applies
   * when TestNG skips the method because of its dependencies.
   */
  public Boolean includeAllDataDrivenTestsWhenSkipping = false;

  /**
   * What TestNG does with a test method whose data provider returns no rows. {@code SKIP} reports
   * the method as skipped. {@code IGNORE} leaves it out of the results.
   */
  public @Nullable EmptyDataProviderBehavior emptyDataProviderBehavior;

  public static final String PROPAGATE_DATA_PROVIDER_FAILURES_AS_TEST_FAILURE =
      "-propagateDataProviderFailureAsTestFailure";

  /**
   * Whether TestNG reports a data provider failure as a test failure.
   *
   * <p>{@link TestNG#configure(CommandLineArgs)} turns this setting on whenever this field is not
   * {@code null}. So the default value, {@code false}, also turns it on.
   */
  public Boolean propagateDataProviderFailureAsTestFailure = false;

  public static final String GENERATE_RESULTS_PER_SUITE = "-generateResultsPerSuite";

  /** Whether TestNG writes the results of each suite to its own directory. */
  public Boolean generateResultsPerSuite = false;

  public static final String SHARE_THREAD_POOL_FOR_DATA_PROVIDERS =
      "-shareThreadPoolForDataProviders";

  /** Whether the data providers of a suite share one thread pool. */
  public Boolean shareThreadPoolForDataProviders = false;

  public static final String USE_GLOBAL_THREAD_POOL = "-useGlobalThreadPool";

  /** Whether the tests of a suite, with or without a data provider, share one thread pool. */
  public Boolean useGlobalThreadPool = false;
}
