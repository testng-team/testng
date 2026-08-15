package org.testng.cli.jcommander;

import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.testng.cli.CliOptions;
import org.testng.xml.XmlSuite;

/**
 * The JCommander description of the TestNG command line. Its only job is to be filled by JCommander
 * and handed over as a {@link CliOptions}.
 *
 * @since 7.13
 */
public class JCommanderOptions {

  @Parameter(description = "The XML suite files to run")
  public List<String> suiteFiles = new ArrayList<>();

  @Parameter(
      names = {CliOptions.LOG, CliOptions.VERBOSE},
      description = "Level of verbosity")
  public @Nullable Integer verbose;

  @Parameter(
      names = CliOptions.GROUPS,
      description = "Comma-separated list of group names to be run")
  public @Nullable String groups;

  @Parameter(
      names = CliOptions.EXCLUDED_GROUPS,
      description = "Comma-separated list of group names to " + " exclude")
  public @Nullable String excludedGroups;

  @Parameter(names = CliOptions.OUTPUT_DIRECTORY, description = "Output directory")
  public @Nullable String outputDirectory;

  /** Accepted for backwards compatibility; TestNG has never acted on it. */
  @Parameter(
      names = CliOptions.MIXED,
      description =
          "No-op since JUnit execution support was removed in 7.10.0."
              + " Kept for command line backward compatibility.")
  public Boolean mixed = Boolean.FALSE;

  @Parameter(
      names = CliOptions.LISTENER,
      description =
          "List of .class files or list of class names"
              + " implementing ITestListener or ISuiteListener")
  public @Nullable String listener;

  @Parameter(
      names = CliOptions.LISTENER_COMPARATOR,
      description =
          "An implementation of ListenerComparator that will be used by TestNG to determine order of execution for listeners")
  public @Nullable String listenerComparator;

  @Parameter(
      names = CliOptions.METHOD_SELECTORS,
      description = "List of .class files or list of class " + "names implementing IMethodSelector")
  public @Nullable String methodSelectors;

  @Parameter(
      names = CliOptions.OBJECT_FACTORY,
      description =
          "Fully qualified class name that implements org.testng.ITestObjectFactory which can be used to create test class and listener instances.")
  public @Nullable String objectFactory;

  @Parameter(names = CliOptions.PARALLEL, description = "Parallel mode (methods, tests or classes)")
  public XmlSuite.@Nullable ParallelMode parallelMode;

  @Parameter(
      names = CliOptions.CONFIG_FAILURE_POLICY,
      description = "Configuration failure policy (skip or continue)")
  public @Nullable String configFailurePolicy;

  @Parameter(
      names = CliOptions.THREAD_COUNT,
      description = "Number of threads to use when running tests " + "in parallel")
  public @Nullable Integer threadCount;

  @Parameter(
      names = CliOptions.DATA_PROVIDER_THREAD_COUNT,
      description = "Number of threads to use when " + "running data providers")
  public @Nullable Integer dataProviderThreadCount;

  @Parameter(
      names = CliOptions.SUITE_NAME,
      description =
          "Default name of test suite, if not specified "
              + "in suite definition file or source code")
  public @Nullable String suiteName;

  @Parameter(
      names = CliOptions.TEST_NAME,
      description =
          "Default name of test, if not specified in suite" + "definition file or source code")
  public @Nullable String testName;

  @Parameter(
      names = CliOptions.REPORTER,
      description = "Extended configuration for custom report listener")
  public @Nullable String reporter;

  @Parameter(
      names = CliOptions.USE_DEFAULT_LISTENERS,
      description = "Whether to use the default listeners")
  public String useDefaultListeners = "true";

  @Parameter(names = CliOptions.SKIP_FAILED_INVOCATION_COUNTS, hidden = true)
  public @Nullable Boolean skipFailedInvocationCounts;

  @Parameter(names = CliOptions.TEST_CLASS, description = "The list of test classes")
  public @Nullable String testClass;

  @Parameter(names = CliOptions.TEST_NAMES, description = "The list of test names to run")
  public @Nullable String testNames;

  @Parameter(
      names = CliOptions.IGNORE_MISSED_TEST_NAMES,
      description =
          "Ignore missed test names given by '-testnames' and continue to run existing tests, if any.")
  public boolean ignoreMissedTestNames = false;

  @Parameter(names = CliOptions.TEST_JAR, description = "A jar file containing the tests")
  public @Nullable String testJar;

  @Parameter(
      names = CliOptions.XML_PATH_IN_JAR,
      description =
          "The full path to the xml file inside the jar file (only valid if -testjar was specified)")
  public String xmlPathInJar = CliOptions.XML_PATH_IN_JAR_DEFAULT;

  @Parameter(
      names = {CliOptions.TEST_RUNNER_FACTORY, "-testRunFactory"},
      description = "The factory used to create tests")
  public @Nullable String testRunnerFactory;

  @Parameter(
      names = CliOptions.LISTENER_FACTORY,
      description = "The factory used to create TestNG listeners")
  public @Nullable String listenerFactory;

  @Parameter(names = CliOptions.METHODS, description = "Comma separated of test methods")
  public List<String> commandLineMethods = new ArrayList<>();

  @Parameter(
      names = CliOptions.SUITE_THREAD_POOL_SIZE,
      description = "Size of the thread pool to use to run suites")
  public Integer suiteThreadPoolSize = CliOptions.SUITE_THREAD_POOL_SIZE_DEFAULT;

  @Parameter(
      names = CliOptions.RANDOMIZE_SUITES,
      hidden = true,
      description = "Whether to run suites in same order as specified in XML or not")
  public Boolean randomizeSuites = Boolean.FALSE;

  @Parameter(
      names = CliOptions.ALWAYS_RUN_LISTENERS,
      description = "Should MethodInvocation Listeners be run even for skipped methods")
  public Boolean alwaysRunListeners = Boolean.TRUE;

  @Parameter(
      names = CliOptions.THREAD_POOL_FACTORY_CLASS,
      description = "The threadpool executor factory implementation that TestNG should use.")
  public @Nullable String threadPoolFactoryClass;

  @Parameter(
      names = CliOptions.DEPENDENCY_INJECTOR_FACTORY,
      description = "The dependency injector factory implementation that TestNG should use.")
  public @Nullable String dependencyInjectorFactoryClass;

  @Parameter(
      names = CliOptions.FAIL_IF_ALL_TESTS_SKIPPED,
      description = "Should TestNG fail execution if all tests were skipped and nothing was run.")
  public Boolean failIfAllTestsSkipped = false;

  @Parameter(
      names = CliOptions.LISTENERS_TO_SKIP_VIA_SPI,
      description =
          "Comma separated fully qualified class names of listeners that should be skipped from being wired in via Service Loaders.")
  public String spiListenersToSkip = "";

  @Parameter(
      names = CliOptions.OVERRIDE_INCLUDED_METHODS,
      description =
          "Whether command line method inclusions override the ones declared in the suite XML.")
  public Boolean overrideIncludedMethods = false;

  @Parameter(
      names = CliOptions.INCLUDE_ALL_DATA_DRIVEN_TESTS_WHEN_SKIPPING,
      description =
          "Should TestNG report all iterations of a data driven test as individual skips, in-case of upstream failures.")
  public Boolean includeAllDataDrivenTestsWhenSkipping = false;

  @Parameter(
      names = CliOptions.PROPAGATE_DATA_PROVIDER_FAILURES_AS_TEST_FAILURE,
      description = "Should TestNG consider failures in Data Providers  as test failures.")
  public Boolean propagateDataProviderFailureAsTestFailure = false;

  @Parameter(
      names = CliOptions.GENERATE_RESULTS_PER_SUITE,
      description =
          "Should TestNG generate results on a per suite basis by creating a sub directory for each suite and dumping results into it.")
  public Boolean generateResultsPerSuite = false;

  @Parameter(
      names = CliOptions.SHARE_THREAD_POOL_FOR_DATA_PROVIDERS,
      description =
          "Should TestNG use a global Shared ThreadPool (At suite level) for running data providers.")
  public Boolean shareThreadPoolForDataProviders = false;

  @Parameter(
      names = CliOptions.USE_GLOBAL_THREAD_POOL,
      description =
          "Should TestNG use a global Shared ThreadPool (At suite level) for running regular and data driven tests.")
  public Boolean useGlobalThreadPool = false;

  /**
   * Converts this command line into the parser agnostic representation.
   *
   * @return the values TestNG configures itself from.
   */
  public CliOptions toCliOptions() {
    CliOptions cli = new CliOptions();
    // Copied rather than aliased: the returned options outlive this parser, and TestNG stores the
    // lists by reference, so sharing them would let a later mutation change a running suite.
    cli.suiteFiles = new ArrayList<>(suiteFiles);
    cli.verbose = verbose;
    cli.groups = groups;
    cli.excludedGroups = excludedGroups;
    cli.outputDirectory = outputDirectory;
    cli.listener = listener;
    cli.listenerComparator = listenerComparator;
    cli.methodSelectors = methodSelectors;
    cli.objectFactory = objectFactory;
    cli.parallelMode = parallelMode;
    cli.configFailurePolicy = configFailurePolicy;
    cli.threadCount = threadCount;
    cli.dataProviderThreadCount = dataProviderThreadCount;
    cli.suiteName = suiteName;
    cli.testName = testName;
    cli.reporter = reporter;
    cli.useDefaultListeners = useDefaultListeners;
    cli.skipFailedInvocationCounts = skipFailedInvocationCounts;
    cli.testClass = testClass;
    cli.testNames = testNames;
    cli.ignoreMissedTestNames = ignoreMissedTestNames;
    cli.testJar = testJar;
    cli.xmlPathInJar = xmlPathInJar;
    cli.testRunnerFactory = testRunnerFactory;
    cli.listenerFactory = listenerFactory;
    cli.commandLineMethods = new ArrayList<>(commandLineMethods);
    cli.suiteThreadPoolSize = suiteThreadPoolSize;
    cli.randomizeSuites = randomizeSuites;
    cli.alwaysRunListeners = alwaysRunListeners;
    cli.threadPoolFactoryClass = threadPoolFactoryClass;
    cli.dependencyInjectorFactoryClass = dependencyInjectorFactoryClass;
    cli.failIfAllTestsSkipped = failIfAllTestsSkipped;
    cli.spiListenersToSkip = spiListenersToSkip;
    cli.overrideIncludedMethods = overrideIncludedMethods;
    cli.includeAllDataDrivenTestsWhenSkipping = includeAllDataDrivenTestsWhenSkipping;
    cli.propagateDataProviderFailureAsTestFailure = propagateDataProviderFailureAsTestFailure;
    cli.generateResultsPerSuite = generateResultsPerSuite;
    cli.shareThreadPoolForDataProviders = shareThreadPoolForDataProviders;
    cli.useGlobalThreadPool = useGlobalThreadPool;
    return cli;
  }
}
