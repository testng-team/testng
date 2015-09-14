package org.testng;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;

import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;

import java.util.ArrayList;
import java.util.List;

public class CommandLineArgs {

  @Parameter(description = "The XML suite files to run")
  public List<String> suiteFiles = Lists.newArrayList();

  public static final String LOG = "-log";
  public static final String VERBOSE = "-verbose";
  @Parameter(names = { LOG, VERBOSE }, description = "Level of verbosity")
  public Integer verbose;

  public static final String GROUPS = "-groups";
  @Parameter(names = GROUPS, description = "Comma-separated list of group names to be run")
  public String groups;

  public static final String EXCLUDED_GROUPS = "-excludegroups";
  @Parameter(names = EXCLUDED_GROUPS, description ="Comma-separated list of group names to "
      + " exclude")
  public String excludedGroups;

  public static final String OUTPUT_DIRECTORY = "-d";
  @Parameter(names = OUTPUT_DIRECTORY, description ="Output directory")
  public String outputDirectory;

  public static final String JUNIT = "-junit";
  @Parameter(names = JUNIT, description ="JUnit mode")
  public Boolean junit = Boolean.FALSE;

  public static final String MIXED = "-mixed";
  @Parameter(names = MIXED, description ="Mixed mode - autodetect the type of current test" +
      " and run it with appropriate runner")
  public Boolean mixed = Boolean.FALSE;

  public static final String LISTENER = "-listener";
  @Parameter(names = LISTENER, description = "List of .class files or list of class names" +
      " implementing ITestListener or ISuiteListener")
  public String listener;

  public static final String METHOD_SELECTORS = "-methodselectors";
  @Parameter(names = METHOD_SELECTORS, description = "List of .class files or list of class " +
        "names implementing IMethodSelector")
  public String methodSelectors;

  public static final String OBJECT_FACTORY = "-objectfactory";
  @Parameter(names = OBJECT_FACTORY, description = "List of .class files or list of class " +
        "names implementing ITestRunnerFactory")
  public String objectFactory;

  public static final String PARALLEL= "-parallel";
  @Parameter(names = PARALLEL, description = "Parallel mode (methods, tests or classes)")
  public XmlSuite.ParallelMode parallelMode;

  public static final String CONFIG_FAILURE_POLICY = "-configfailurepolicy";
  @Parameter(names = CONFIG_FAILURE_POLICY , description = "Configuration failure policy (skip or continue)")
  public String configFailurePolicy;

  public static final String THREAD_COUNT = "-threadcount";
  @Parameter(names = THREAD_COUNT, description = "Number of threads to use when running tests " +
      "in parallel")
  public Integer threadCount;

  public static final String DATA_PROVIDER_THREAD_COUNT = "-dataproviderthreadcount";
  @Parameter(names = DATA_PROVIDER_THREAD_COUNT, description = "Number of threads to use when " +
      "running data providers")
  public Integer dataProviderThreadCount;

  public static final String SUITE_NAME = "-suitename";
  @Parameter(names = SUITE_NAME, description = "Default name of test suite, if not specified " +
      "in suite definition file or source code")
  public String suiteName;

  public static final String TEST_NAME = "-testname";
  @Parameter(names = TEST_NAME, description = "Default name of test, if not specified in suite" +
      "definition file or source code")
  public String testName;

  public static final String REPORTER = "-reporter";
  @Parameter(names = REPORTER, description = "Extended configuration for custom report listener")
  public String reporter;

  public static final String USE_DEFAULT_LISTENERS = "-usedefaultlisteners";
  @Parameter(names = USE_DEFAULT_LISTENERS, description = "Whether to use the default listeners")
  public String useDefaultListeners = "true";

  public static final String SKIP_FAILED_INVOCATION_COUNTS = "-skipfailedinvocationcounts";
  @Parameter(names = SKIP_FAILED_INVOCATION_COUNTS, hidden = true)
  public Boolean skipFailedInvocationCounts;

  public static final String TEST_CLASS = "-testclass";
  @Parameter(names = TEST_CLASS, description = "The list of test classes")
  public String testClass;

  public static final String TEST_NAMES = "-testnames";
  @Parameter(names = TEST_NAMES, description = "The list of test names to run")
  public String testNames;

  public static final String TEST_JAR = "-testjar";
  @Parameter(names = TEST_JAR, description = "A jar file containing the tests")
  public String testJar;

  public static final String XML_PATH_IN_JAR = "-xmlpathinjar";
  public static final String XML_PATH_IN_JAR_DEFAULT = "testng.xml";
  @Parameter(names = XML_PATH_IN_JAR, description = "The full path to the xml file inside the jar file (only valid if -testjar was specified)")
  public String xmlPathInJar = XML_PATH_IN_JAR_DEFAULT;

  public static final String TEST_RUNNER_FACTORY = "-testrunfactory";
  @Parameter(names = { TEST_RUNNER_FACTORY, "-testRunFactory" },
      description = "The factory used to create tests")
  public String testRunnerFactory;

  public static final String PORT = "-port";
  @Parameter(names = PORT, description = "The port")
  public Integer port;

  public static final String HOST = "-host";
  @Parameter(names = HOST, description = "The host", hidden = true)
  public String host;

  public static final String MASTER = "-master";
  @Parameter(names = MASTER, description = "Host where the master is", hidden = true)
  public String master;

  public static final String SLAVE = "-slave";
  @Parameter(names = SLAVE, description = "Host where the slave is", hidden = true)
  public String slave;

  public static final String METHODS = "-methods";
  @Parameter(names = METHODS, description = "Comma separated of test methods",
      splitter = CommaParameterSplitter.class)
  public List<String> commandLineMethods = new ArrayList<>();

  public static final String SUITE_THREAD_POOL_SIZE = "-suitethreadpoolsize";
  public static final Integer SUITE_THREAD_POOL_SIZE_DEFAULT = 1;
  @Parameter(names = SUITE_THREAD_POOL_SIZE, description = "Size of the thread pool to use"
        + " to run suites")
  public Integer suiteThreadPoolSize = SUITE_THREAD_POOL_SIZE_DEFAULT;

  public static final String RANDOMIZE_SUITES = "-randomizesuites";
  @Parameter(names = RANDOMIZE_SUITES, hidden = true,
      description = "Whether to run suites in same order as specified in XML or not")
  public Boolean randomizeSuites = Boolean.FALSE;

  public static final String DEBUG = "-debug";
  @Parameter(names = DEBUG, hidden = true, description = "Used to debug TestNG")
  public Boolean debug = Boolean.FALSE;

}
