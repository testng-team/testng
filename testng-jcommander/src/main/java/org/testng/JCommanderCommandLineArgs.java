package org.testng;

import com.beust.jcommander.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.collections.Lists;
import org.testng.internal.ClassHelper;
import org.testng.internal.ReporterConfig;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

public class JCommanderCommandLineArgs extends AbstractCommandLineArgs {

  @Parameter(description = "The XML suite files to run")
  private List<String> suiteFiles = Lists.newArrayList();

  @Parameter(
      names = {LOG, VERBOSE},
      description = "Level of verbosity")
  private Integer verbose;

  @Parameter(names = GROUPS, description = "Comma-separated list of group names to be run")
  private String groups;

  @Parameter(
      names = EXCLUDED_GROUPS,
      description = "Comma-separated list of group names to exclude")
  private String excludedGroups;

  @Parameter(names = OUTPUT_DIRECTORY, description = "Output directory")
  private String outputDirectory;

  @Parameter(names = JUNIT, description = "JUnit mode")
  public Boolean junit = Boolean.FALSE;

  @Parameter(
      names = MIXED,
      description =
          "Mixed mode - autodetect the type of current test"
              + " and run it with appropriate runner")
  private Boolean mixed = Boolean.FALSE;

  @Parameter(
      names = LISTENER,
      description =
          "List of .class files or list of class names"
              + " implementing ITestListener or ISuiteListener")
  private String listener;

  @Parameter(
      names = METHOD_SELECTORS,
      description = "List of .class files or list of class names implementing IMethodSelector")
  private String methodSelectors;

  @Parameter(
      names = OBJECT_FACTORY,
      description = "List of .class files or list of class names implementing ITestRunnerFactory")
  private String objectFactory;

  @Parameter(names = PARALLEL, description = "Parallel mode (methods, tests or classes)")
  private XmlSuite.ParallelMode parallelMode;

  @Parameter(
      names = CONFIG_FAILURE_POLICY,
      description = "Configuration failure policy (skip or continue)")
  private String configFailurePolicy;

  @Parameter(
      names = THREAD_COUNT,
      description = "Number of threads to use when running tests in parallel")
  private Integer threadCount;

  @Parameter(
      names = DATA_PROVIDER_THREAD_COUNT,
      description = "Number of threads to use when running data providers")
  private Integer dataProviderThreadCount;

  @Parameter(
      names = SUITE_NAME,
      description =
          "Default name of test suite, if not specified in suite definition file or source code")
  private String suiteName;

  @Parameter(
      names = TEST_NAME,
      description =
          "Default name of test, if not specified in suite definition file or source code")
  private String testName;

  @Parameter(names = REPORTER, description = "Extended configuration for custom report listener")
  private String reporter;

  // Note: can't use a Boolean field here because we are allowing a boolean
  // parameter with an arity of 1 ("-usedefaultlisteners false")
  @Parameter(names = USE_DEFAULT_LISTENERS, description = "Whether to use the default listeners")
  private String useDefaultListeners = "true";

  @Parameter(names = SKIP_FAILED_INVOCATION_COUNTS, hidden = true)
  private Boolean skipFailedInvocationCounts;

  @Parameter(names = TEST_CLASS, description = "The list of test classes")
  private String testClass;

  @Parameter(names = TEST_NAMES, description = "The list of test names to run")
  private String testNames;

  @Parameter(names = TEST_JAR, description = "A jar file containing the tests")
  private String testJar;

  @Parameter(
      names = XML_PATH_IN_JAR,
      description =
          "The full path to the xml file inside the jar file (only valid if -testjar was specified)")
  private String xmlPathInJar = TestNG.XML_PATH_IN_JAR_DEFAULT;

  @Parameter(
      names = {TEST_RUNNER_FACTORY, "-testRunFactory"},
      description = "The factory used to create tests")
  private String testRunnerFactory;

  @Parameter(names = PORT, description = "The port")
  private Integer port;

  @Parameter(names = HOST, description = "The host", hidden = true)
  private String host;

  @Parameter(names = METHODS, description = "Comma separated of test methods")
  private List<String> commandLineMethods = new ArrayList<>();

  @Parameter(
      names = SUITE_THREAD_POOL_SIZE,
      description = "Size of the thread pool to use" + " to run suites")
  private Integer suiteThreadPoolSize = SUITE_THREAD_POOL_SIZE_DEFAULT;

  @Parameter(
      names = RANDOMIZE_SUITES,
      hidden = true,
      description = "Whether to run suites in same order as specified in XML or not")
  private Boolean randomizeSuites = Boolean.FALSE;

  @Parameter(names = DEBUG, hidden = true, description = "Used to debug TestNG")
  private Boolean debug = Boolean.FALSE;

  @Parameter(
      names = ALWAYS_RUN_LISTENERS,
      description = "Should MethodInvocation Listeners be run even for skipped methods")
  private Boolean alwaysRunListeners = Boolean.TRUE;

  @Parameter(
      names = THREAD_POOL_FACTORY_CLASS,
      description = "The threadpool executor factory implementation that TestNG should use.")
  private String threadPoolFactoryClass;

  @Parameter(
      names = DEPENDENCY_INJECTOR_FACTORY,
      description = "The dependency injector factory implementation that TestNG should use.")
  private String dependencyInjectorFactoryClass;

  @Parameter(
      names = FAIL_IF_ALL_TESTS_SKIPPED,
      description = "Should TestNG fail execution if all tests were skipped and nothing was run.")
  private Boolean failIfAllTestsSkipped = false;

  @Parameter(
      names = LISTENERS_TO_SKIP_VIA_SPI,
      description =
          "Comma separated fully qualified class names of listeners that should be skipped from being wired in via Service Loaders.")
  private String spiListenersToSkip = "";

  @Parameter(
      names = OVERRIDE_INCLUDED_METHODS,
      description =
          "Comma separated fully qualified class names of listeners that should be skipped from being wired in via Service Loaders.")
  private Boolean overrideIncludedMethods = false;

  @Override
  public List<String> getSuiteFiles() {
    return suiteFiles;
  }

  @Override
  public Integer getVerbose() {
    return verbose;
  }

  @Override
  public String getGroups() {
    return groups;
  }

  @Override
  public String getExcludedGroups() {
    return excludedGroups;
  }

  @Override
  public String getOutputDirectory() {
    return outputDirectory;
  }

  @Override
  public Boolean isJUnit() {
    return junit;
  }

  @Override
  public Boolean isMixed() {
    return mixed;
  }

  @Override
  protected String[] getListenerValues() {
    if (listener == null) {
      return null;
    }
    String sep = ";";
    if (listener.contains(",")) {
      sep = ",";
    }
    return Utils.split(listener, sep);
  }

  @Override
  protected String getMethodSelectorsValue() {
    return methodSelectors;
  }

  @Override
  protected String getObjectFactoryValue() {
    return objectFactory;
  }

  @Override
  public XmlSuite.ParallelMode getParallelMode() {
    return parallelMode;
  }

  @Override
  public XmlSuite.FailurePolicy getConfigFailurePolicy() {
    return XmlSuite.FailurePolicy.getValidPolicy(configFailurePolicy);
  }

  @Override
  public Integer getThreadCount() {
    return threadCount;
  }

  @Override
  public Integer getDataProviderThreadCount() {
    return dataProviderThreadCount;
  }

  @Override
  public String getSuiteName() {
    return suiteName;
  }

  @Override
  public String getTestName() {
    return testName;
  }

  @Override
  public ReporterConfig getReporter() {
    return ReporterConfig.deserialize(reporter);
  }

  @Override
  public Boolean useDefaultListeners() {
    if (useDefaultListeners == null) {
      return null;
    }
    return "true".equalsIgnoreCase(useDefaultListeners);
  }

  @Override
  public Boolean skipFailedInvocationCounts() {
    return skipFailedInvocationCounts;
  }

  @Override
  protected String getTestClassValue() {
    return testClass;
  }

  @Override
  protected String getTestNamesValue() {
    return testNames;
  }

  @Override
  public String getTestJar() {
    return testJar;
  }

  @Override
  public String getXmlPathInJar() {
    return xmlPathInJar;
  }

  @Override
  protected String getTestRunnerFactoryValue() {
    return testRunnerFactory;
  }

  @Override
  public Integer getPort() {
    return port;
  }

  @Override
  public String getHost() {
    return host;
  }

  @Override
  public List<String> getCommandLineMethods() {
    return commandLineMethods;
  }

  @Override
  public Integer getSuiteThreadPoolSize() {
    return suiteThreadPoolSize;
  }

  @Override
  public Boolean isRandomizeSuites() {
    return randomizeSuites;
  }

  @Override
  public Boolean isDebug() {
    return debug;
  }

  @Override
  public Boolean alwaysRunListeners() {
    return alwaysRunListeners;
  }

  @Override
  public String getThreadPoolFactoryClass() {
    return threadPoolFactoryClass;
  }

  @Override
  public Class<IInjectorFactory> getDependencyInjectorFactory() {
    if (dependencyInjectorFactoryClass == null) {
      return null;
    }
    return (Class<IInjectorFactory>) ClassHelper.forName(dependencyInjectorFactoryClass);
  }

  @Override
  public Boolean failIfAllTestsSkipped() {
    return failIfAllTestsSkipped;
  }

  @Override
  protected String getSpiListenersToSkipValue() {
    return spiListenersToSkip;
  }

  @Override
  public Boolean overrideIncludedMethods() {
    return overrideIncludedMethods;
  }
}
