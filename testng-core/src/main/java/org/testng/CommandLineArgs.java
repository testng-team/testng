package org.testng;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.xml.XmlSuite;

/**
 * The values the TestNG command line used to be parsed into.
 *
 * <p>Since 7.13 the command line front end lives in the {@code testng-cli} and {@code
 * testng-jcommander} modules, so that {@code testng-core} no longer depends on a command line
 * parsing library. This class is kept, without its JCommander annotations, only so that {@link
 * TestNG#configure(CommandLineArgs)}, {@link TestNG#configure(java.util.Map)} and subclasses such
 * as {@code RemoteTestNG} keep working. It is no longer populated by TestNG itself.
 *
 * @deprecated since 7.13. Use {@code org.testng.cli.CliOptions} from the {@code testng-cli} module.
 *     Scheduled for removal in 8.0.
 */
@Deprecated
public class CommandLineArgs {

  /** The XML suite files to run. */
  public List<String> suiteFiles = new ArrayList<>();

  public static final String LOG = "-log";
  public static final String VERBOSE = "-verbose";

  /** Level of verbosity. */
  public @Nullable Integer verbose;

  public static final String GROUPS = "-groups";

  /** Comma-separated list of group names to be run. */
  public @Nullable String groups;

  public static final String EXCLUDED_GROUPS = "-excludegroups";

  /** Comma-separated list of group names to exclude. */
  public @Nullable String excludedGroups;

  public static final String OUTPUT_DIRECTORY = "-d";

  /** Output directory. */
  public @Nullable String outputDirectory;

  public static final String MIXED = "-mixed";

  /**
   * No-op since JUnit execution support was removed in 7.10.0. Kept for command line backward
   * compatibility.
   */
  public Boolean mixed = Boolean.FALSE;

  public static final String LISTENER = "-listener";

  /** List of .class files or list of class names implementing ITestListener or ISuiteListener. */
  public @Nullable String listener;

  public static final String LISTENER_COMPARATOR = "-listenercomparator";

  /** An implementation of ListenerComparator that determines order of execution for listeners. */
  public @Nullable String listenerComparator;

  public static final String METHOD_SELECTORS = "-methodselectors";

  /** List of .class files or list of class names implementing IMethodSelector. */
  public @Nullable String methodSelectors;

  public static final String OBJECT_FACTORY = "-objectfactory";

  /** Fully qualified class name that implements org.testng.ITestObjectFactory. */
  public @Nullable String objectFactory;

  public static final String PARALLEL = "-parallel";

  /** Parallel mode (methods, tests or classes). */
  public XmlSuite.@Nullable ParallelMode parallelMode;

  public static final String CONFIG_FAILURE_POLICY = "-configfailurepolicy";

  /** Configuration failure policy (skip or continue). */
  public @Nullable String configFailurePolicy;

  public static final String THREAD_COUNT = "-threadcount";

  /** Number of threads to use when running tests in parallel. */
  public @Nullable Integer threadCount;

  public static final String DATA_PROVIDER_THREAD_COUNT = "-dataproviderthreadcount";

  /** Number of threads to use when running data providers. */
  public @Nullable Integer dataProviderThreadCount;

  public static final String SUITE_NAME = "-suitename";

  /** Default name of test suite, if not specified in suite definition file or source code. */
  public @Nullable String suiteName;

  public static final String TEST_NAME = "-testname";

  /** Default name of test, if not specified in suite definition file or source code. */
  public @Nullable String testName;

  public static final String REPORTER = "-reporter";

  /** Extended configuration for custom report listener. */
  public @Nullable String reporter;

  public static final String USE_DEFAULT_LISTENERS = "-usedefaultlisteners";

  /** Whether to use the default listeners. */
  public String useDefaultListeners = "true";

  public static final String SKIP_FAILED_INVOCATION_COUNTS = "-skipfailedinvocationcounts";

  public @Nullable Boolean skipFailedInvocationCounts;

  public static final String TEST_CLASS = "-testclass";

  /** The list of test classes. */
  public @Nullable String testClass;

  public static final String TEST_NAMES = "-testnames";

  /** The list of test names to run. */
  public @Nullable String testNames;

  public static final String IGNORE_MISSED_TEST_NAMES = "-ignoreMissedTestNames";

  /** Ignore missed test names given by '-testnames' and continue to run existing tests, if any. */
  public boolean ignoreMissedTestNames = false;

  public static final String TEST_JAR = "-testjar";

  /** A jar file containing the tests. */
  public @Nullable String testJar;

  public static final String XML_PATH_IN_JAR = "-xmlpathinjar";
  public static final String XML_PATH_IN_JAR_DEFAULT = "testng.xml";

  /** The full path to the xml file inside the jar file, only valid with -testjar. */
  public String xmlPathInJar = XML_PATH_IN_JAR_DEFAULT;

  public static final String TEST_RUNNER_FACTORY = "-testrunfactory";

  /** The factory used to create tests. */
  public @Nullable String testRunnerFactory;

  public static final String LISTENER_FACTORY = "-listenerfactory";

  /** The factory used to create TestNG listeners. */
  public @Nullable String listenerFactory;

  public static final String METHODS = "-methods";

  /** Comma separated list of test methods. */
  public List<String> commandLineMethods = new ArrayList<>();

  public static final String SUITE_THREAD_POOL_SIZE = "-suitethreadpoolsize";
  public static final Integer SUITE_THREAD_POOL_SIZE_DEFAULT = 1;

  /** Size of the thread pool to use to run suites. */
  public Integer suiteThreadPoolSize = SUITE_THREAD_POOL_SIZE_DEFAULT;

  public static final String RANDOMIZE_SUITES = "-randomizesuites";

  /** Whether to run suites in same order as specified in XML or not. */
  public Boolean randomizeSuites = Boolean.FALSE;

  public static final String ALWAYS_RUN_LISTENERS = "-alwaysrunlisteners";

  /** Should MethodInvocation Listeners be run even for skipped methods. */
  public Boolean alwaysRunListeners = Boolean.TRUE;

  public static final String THREAD_POOL_FACTORY_CLASS = "-threadpoolfactoryclass";

  /** The threadpool executor factory implementation that TestNG should use. */
  public @Nullable String threadPoolFactoryClass;

  public static final String DEPENDENCY_INJECTOR_FACTORY = "-dependencyinjectorfactory";

  /** The dependency injector factory implementation that TestNG should use. */
  public @Nullable String dependencyInjectorFactoryClass;

  public static final String FAIL_IF_ALL_TESTS_SKIPPED = "-failwheneverythingskipped";

  /** Should TestNG fail execution if all tests were skipped and nothing was run. */
  public Boolean failIfAllTestsSkipped = false;

  public static final String LISTENERS_TO_SKIP_VIA_SPI = "-spilistenerstoskip";

  /**
   * Comma separated fully qualified class names of listeners that should be skipped from being
   * wired in via Service Loaders.
   */
  public String spiListenersToSkip = "";

  public static final String OVERRIDE_INCLUDED_METHODS = "-overrideincludedmethods";

  /** Whether command line method inclusions override the ones declared in the suite XML. */
  public Boolean overrideIncludedMethods = false;

  public static final String INCLUDE_ALL_DATA_DRIVEN_TESTS_WHEN_SKIPPING =
      "-includeAllDataDrivenTestsWhenSkipping";

  /**
   * Should TestNG report all iterations of a data driven test as individual skips, in-case of
   * upstream failures.
   */
  public Boolean includeAllDataDrivenTestsWhenSkipping = false;

  public static final String PROPAGATE_DATA_PROVIDER_FAILURES_AS_TEST_FAILURE =
      "-propagateDataProviderFailureAsTestFailure";

  /** Should TestNG consider failures in Data Providers as test failures. */
  public Boolean propagateDataProviderFailureAsTestFailure = false;

  public static final String GENERATE_RESULTS_PER_SUITE = "-generateResultsPerSuite";

  /** Should TestNG generate results in a sub directory per suite. */
  public Boolean generateResultsPerSuite = false;

  public static final String SHARE_THREAD_POOL_FOR_DATA_PROVIDERS =
      "-shareThreadPoolForDataProviders";

  /** Should TestNG use a global shared thread pool for running data providers. */
  public Boolean shareThreadPoolForDataProviders = false;

  public static final String USE_GLOBAL_THREAD_POOL = "-useGlobalThreadPool";

  /** Should TestNG use a global shared thread pool for regular and data driven tests. */
  public Boolean useGlobalThreadPool = false;
}
