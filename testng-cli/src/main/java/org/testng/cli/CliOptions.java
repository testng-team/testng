package org.testng.cli;

import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.xml.XmlSuite;

/**
 * The values a command line parser extracts from {@code argv}, before they are turned into a {@link
 * org.testng.TestNG} configuration by {@link CliConfigurer}.
 *
 * <p>Fields hold values as close to the raw command line as possible, so that a parser only has to
 * fill this object and never has to know about class loading, listener wiring or reporter
 * configuration. The single exception is {@link #parallelMode}, which is typed so that parsers
 * reject an invalid value instead of silently falling back to {@link XmlSuite.ParallelMode#NONE}.
 *
 * <p>The {@code String} constants are the canonical option names. They are duplicated in the
 * deprecated {@code org.testng.CommandLineArgs}, which the Maven Surefire integration still reads;
 * {@code CliOptionNamesTest} guards the two from drifting apart.
 *
 * @since 7.13
 */
public class CliOptions {

  public static final String LOG = "-log";
  public static final String VERBOSE = "-verbose";
  public static final String GROUPS = "-groups";
  public static final String EXCLUDED_GROUPS = "-excludegroups";
  public static final String OUTPUT_DIRECTORY = "-d";
  public static final String MIXED = "-mixed";
  public static final String LISTENER = "-listener";
  public static final String LISTENER_COMPARATOR = "-listenercomparator";
  public static final String METHOD_SELECTORS = "-methodselectors";
  public static final String OBJECT_FACTORY = "-objectfactory";
  public static final String PARALLEL = "-parallel";
  public static final String CONFIG_FAILURE_POLICY = "-configfailurepolicy";
  public static final String THREAD_COUNT = "-threadcount";
  public static final String DATA_PROVIDER_THREAD_COUNT = "-dataproviderthreadcount";
  public static final String SUITE_NAME = "-suitename";
  public static final String TEST_NAME = "-testname";
  public static final String REPORTER = "-reporter";
  public static final String USE_DEFAULT_LISTENERS = "-usedefaultlisteners";
  public static final String SKIP_FAILED_INVOCATION_COUNTS = "-skipfailedinvocationcounts";
  public static final String TEST_CLASS = "-testclass";
  public static final String TEST_NAMES = "-testnames";
  public static final String IGNORE_MISSED_TEST_NAMES = "-ignoreMissedTestNames";
  public static final String TEST_JAR = "-testjar";
  public static final String XML_PATH_IN_JAR = "-xmlpathinjar";
  public static final String XML_PATH_IN_JAR_DEFAULT = "testng.xml";
  public static final String TEST_RUNNER_FACTORY = "-testrunfactory";
  public static final String LISTENER_FACTORY = "-listenerfactory";
  public static final String METHODS = "-methods";
  public static final String SUITE_THREAD_POOL_SIZE = "-suitethreadpoolsize";
  public static final Integer SUITE_THREAD_POOL_SIZE_DEFAULT = 1;
  public static final String RANDOMIZE_SUITES = "-randomizesuites";
  public static final String ALWAYS_RUN_LISTENERS = "-alwaysrunlisteners";
  public static final String THREAD_POOL_FACTORY_CLASS = "-threadpoolfactoryclass";
  public static final String DEPENDENCY_INJECTOR_FACTORY = "-dependencyinjectorfactory";
  public static final String FAIL_IF_ALL_TESTS_SKIPPED = "-failwheneverythingskipped";
  public static final String LISTENERS_TO_SKIP_VIA_SPI = "-spilistenerstoskip";
  public static final String OVERRIDE_INCLUDED_METHODS = "-overrideincludedmethods";
  public static final String INCLUDE_ALL_DATA_DRIVEN_TESTS_WHEN_SKIPPING =
      "-includeAllDataDrivenTestsWhenSkipping";
  public static final String PROPAGATE_DATA_PROVIDER_FAILURES_AS_TEST_FAILURE =
      "-propagateDataProviderFailureAsTestFailure";
  public static final String GENERATE_RESULTS_PER_SUITE = "-generateResultsPerSuite";
  public static final String SHARE_THREAD_POOL_FOR_DATA_PROVIDERS =
      "-shareThreadPoolForDataProviders";
  public static final String USE_GLOBAL_THREAD_POOL = "-useGlobalThreadPool";

  /** The XML suite files to run. */
  public List<String> suiteFiles = new ArrayList<>();

  /** Level of verbosity. */
  public @Nullable Integer verbose;

  /** Comma-separated list of group names to be run. */
  public @Nullable String groups;

  /** Comma-separated list of group names to exclude. */
  public @Nullable String excludedGroups;

  /** Output directory. */
  public @Nullable String outputDirectory;

  /**
   * List of {@code .class} files or list of class names implementing {@code ITestListener} or
   * {@code ISuiteListener}.
   */
  public @Nullable String listener;

  /** An implementation of {@code ListenerComparator} that orders listener execution. */
  public @Nullable String listenerComparator;

  /** List of {@code .class} files or list of class names implementing {@code IMethodSelector}. */
  public @Nullable String methodSelectors;

  /** Fully qualified class name that implements {@code org.testng.ITestObjectFactory}. */
  public @Nullable String objectFactory;

  /** Parallel mode (methods, tests or classes). */
  public XmlSuite.@Nullable ParallelMode parallelMode;

  /** Configuration failure policy (skip or continue). */
  public @Nullable String configFailurePolicy;

  /** Number of threads to use when running tests in parallel. */
  public @Nullable Integer threadCount;

  /** Number of threads to use when running data providers. */
  public @Nullable Integer dataProviderThreadCount;

  /** Default name of test suite, if not specified in suite definition file or source code. */
  public @Nullable String suiteName;

  /** Default name of test, if not specified in suite definition file or source code. */
  public @Nullable String testName;

  /** Extended configuration for custom report listener. */
  public @Nullable String reporter;

  /**
   * Whether to use the default listeners. This is a {@code String} because the option has an arity
   * of one ({@code -usedefaultlisteners false}).
   */
  public String useDefaultListeners = "true";

  public @Nullable Boolean skipFailedInvocationCounts;

  /** The list of test classes. */
  public @Nullable String testClass;

  /** The list of test names to run. */
  public @Nullable String testNames;

  /** Ignore missed test names given by {@code -testnames} and continue to run existing tests. */
  public boolean ignoreMissedTestNames = false;

  /** A jar file containing the tests. */
  public @Nullable String testJar;

  /** The full path to the xml file inside the jar file, only valid with {@code -testjar}. */
  public String xmlPathInJar = XML_PATH_IN_JAR_DEFAULT;

  /** The factory used to create tests. */
  public @Nullable String testRunnerFactory;

  /** The factory used to create TestNG listeners. */
  public @Nullable String listenerFactory;

  /** Comma separated list of test methods. */
  public List<String> commandLineMethods = new ArrayList<>();

  /** Size of the thread pool to use to run suites. */
  public Integer suiteThreadPoolSize = SUITE_THREAD_POOL_SIZE_DEFAULT;

  /** Whether to run suites in same order as specified in XML or not. */
  public Boolean randomizeSuites = Boolean.FALSE;

  /** Should MethodInvocation Listeners be run even for skipped methods. */
  public Boolean alwaysRunListeners = Boolean.TRUE;

  /** The threadpool executor factory implementation that TestNG should use. */
  public @Nullable String threadPoolFactoryClass;

  /** The dependency injector factory implementation that TestNG should use. */
  public @Nullable String dependencyInjectorFactoryClass;

  /** Should TestNG fail execution if all tests were skipped and nothing was run. */
  public Boolean failIfAllTestsSkipped = false;

  /**
   * Comma separated fully qualified class names of listeners that should be skipped from being
   * wired in via Service Loaders. Never {@code null}: {@link CliConfigurer} splits it eagerly.
   */
  public String spiListenersToSkip = "";

  /** Whether command line method inclusions override the ones declared in the suite XML. */
  public Boolean overrideIncludedMethods = false;

  /**
   * Should TestNG report all iterations of a data driven test as individual skips, in case of
   * upstream failures.
   */
  public Boolean includeAllDataDrivenTestsWhenSkipping = false;

  /** Should TestNG consider failures in Data Providers as test failures. */
  public Boolean propagateDataProviderFailureAsTestFailure = false;

  /** Should TestNG generate results in a sub directory per suite. */
  public Boolean generateResultsPerSuite = false;

  /** Should TestNG use a global shared thread pool for running data providers. */
  public Boolean shareThreadPoolForDataProviders = false;

  /** Should TestNG use a global shared thread pool for regular and data driven tests. */
  public Boolean useGlobalThreadPool = false;
}
