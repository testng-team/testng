package org.testng;

import java.util.List;
import org.testng.internal.ReporterConfig;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlSuite;

/**
 * The command line parameters are:
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
 */
public interface CommandLineArgs {

  String LOG = "-log";

  /** @return The XML suite files to run */
  List<String> getSuiteFiles();

  String VERBOSE = "-verbose";

  /** @return Level of verbosity */
  Integer getVerbose();

  String GROUPS = "-groups";

  /** @return Comma-separated list of group names to be run */
  String getGroups();

  String EXCLUDED_GROUPS = "-excludegroups";

  /** @return Comma-separated list of group names to exclude */
  String getExcludedGroups();

  String OUTPUT_DIRECTORY = "-d";

  /** @return Output directory */
  String getOutputDirectory();

  String JUNIT = "-junit";

  /** @return JUnit mode */
  Boolean isJUnit();

  String MIXED = "-mixed";

  /** @return Mixed mode - autodetect the type of current test and run it with appropriate runner */
  Boolean isMixed();

  String LISTENER = "-listener";

  /**
   * @return List of .class files or list of class names implementing ITestListener or
   *     ISuiteListener
   */
  List<Class<? extends ITestNGListener>> getListener();

  String METHOD_SELECTORS = "-methodselectors";

  /** @return List of .class files or list of class names implementing IMethodSelector */
  List<XmlMethodSelector> getMethodSelectors();

  String OBJECT_FACTORY = "-objectfactory";

  /** @return List of .class files or list of class names implementing ITestRunnerFactory */
  Class<? extends ITestObjectFactory> getObjectFactory();

  String PARALLEL = "-parallel";

  /** @return Parallel mode (methods, tests or classes) */
  XmlSuite.ParallelMode getParallelMode();

  String CONFIG_FAILURE_POLICY = "-configfailurepolicy";

  /** @return Configuration failure policy (skip or continue) */
  XmlSuite.FailurePolicy getConfigFailurePolicy();

  String THREAD_COUNT = "-threadcount";

  /** @return Number of threads to use when running tests in parallel */
  Integer getThreadCount();

  String DATA_PROVIDER_THREAD_COUNT = "-dataproviderthreadcount";

  /** @return Number of threads to use when running data providers */
  Integer getDataProviderThreadCount();

  String SUITE_NAME = "-suitename";

  /**
   * @return Default name of test suite, if not specified in suite definition file or source code
   */
  String getSuiteName();

  String TEST_NAME = "-testname";

  /** @return Default name of test, if not specified in suite definition file or source code */
  String getTestName();

  String REPORTER = "-reporter";

  /** @return Extended configuration for custom report listener */
  ReporterConfig getReporter();

  String USE_DEFAULT_LISTENERS = "-usedefaultlisteners";

  /** @return Whether to use the default listeners */
  Boolean useDefaultListeners();

  String SKIP_FAILED_INVOCATION_COUNTS = "-skipfailedinvocationcounts";

  Boolean skipFailedInvocationCounts();

  String TEST_CLASS = "-testclass";

  /** @return The list of test classes */
  List<Class<?>> getTestClass();

  String TEST_NAMES = "-testnames";

  /** @return The list of test names to run */
  List<String> getTestNames();

  String TEST_JAR = "-testjar";

  /** @return A jar file containing the tests */
  String getTestJar();

  String XML_PATH_IN_JAR = "-xmlpathinjar";

  /**
   * @return The full path to the xml file inside the jar file (only valid if -testjar was
   *     specified)
   */
  String getXmlPathInJar();

  String TEST_RUNNER_FACTORY = "-testrunfactory";

  /** @return The factory used to create tests */
  Class<? extends ITestRunnerFactory> getTestRunnerFactory();

  String PORT = "-port";

  /** @return The port */
  Integer getPort();

  String HOST = "-host";

  /** @return The host */
  String getHost();

  String METHODS = "-methods";

  /** @return Comma separated of test methods */
  List<String> getCommandLineMethods();

  String SUITE_THREAD_POOL_SIZE = "-suitethreadpoolsize";
  Integer SUITE_THREAD_POOL_SIZE_DEFAULT = 1;

  /** @return Size of the thread pool to use to run suites */
  Integer getSuiteThreadPoolSize();

  String RANDOMIZE_SUITES = "-randomizesuites";

  /** @return Whether to run suites in same order as specified in XML or not */
  Boolean isRandomizeSuites();

  String DEBUG = "-debug";

  /** @return Used to debug TestNG */
  Boolean isDebug();

  String ALWAYS_RUN_LISTENERS = "-alwaysrunlisteners";

  /** @return Should MethodInvocation Listeners be run even for skipped methods */
  Boolean alwaysRunListeners();

  String THREAD_POOL_FACTORY_CLASS = "-threadpoolfactoryclass";

  /** @return The threadpool executor factory implementation that TestNG should use. */
  String getThreadPoolFactoryClass();

  String DEPENDENCY_INJECTOR_FACTORY = "-dependencyinjectorfactory";

  /** @return The dependency injector factory implementation that TestNG should use. */
  Class<IInjectorFactory> getDependencyInjectorFactory();

  String FAIL_IF_ALL_TESTS_SKIPPED = "-failwheneverythingskipped";

  /** @return Should TestNG fail execution if all tests were skipped and nothing was run. */
  Boolean failIfAllTestsSkipped();

  String LISTENERS_TO_SKIP_VIA_SPI = "-spilistenerstoskip";

  /**
   * @return Comma separated fully qualified class names of listeners that should be skipped from
   *     being wired in via Service Loaders.
   */
  List<String> getSpiListenersToSkip();

  String OVERRIDE_INCLUDED_METHODS = "-overrideincludedmethods";

  /**
   * @return Comma separated fully qualified class names of listeners that should be skipped from
   *     being wired in via Service Loaders.
   */
  Boolean overrideIncludedMethods();

  default void validate() throws ParameterException {
    List<Class<?>> testClasses = getTestClass();
    List<String> testNgXml = getSuiteFiles();
    String testJar = getTestJar();
    List<String> methods = getCommandLineMethods();

    if (testClasses == null
        && testJar == null
        && (testNgXml == null || testNgXml.isEmpty())
        && (methods == null || methods.isEmpty())) {
      throw new ParameterException(
          "You need to specify at least one testng.xml, one class" + " or one method");
    }

    String groups = getGroups();
    String excludedGroups = getExcludedGroups();

    if (testJar == null
        && (null != groups || null != excludedGroups)
        && testClasses == null
        && (testNgXml == null || testNgXml.isEmpty())) {
      throw new ParameterException("Groups option should be used with testclass option");
    }

    Boolean junit = isJUnit();
    Boolean mixed = isMixed();
    if (junit && mixed) {
      throw new ParameterException(
          CommandLineArgs.MIXED + " can't be combined with " + CommandLineArgs.JUNIT);
    }
  }

  /**
   * Configure the TestNG instance based on the command line parameters.
   *
   * @param tng The TestNG instance
   */
  default void configure(TestNG tng) {
    if (getVerbose() != null) {
      tng.setVerbose(getVerbose());
    }
    if (getDependencyInjectorFactory() != null) {
      tng.setInjectorFactory(getDependencyInjectorFactory());
    }
    if (getThreadPoolFactoryClass() != null) {
      tng.setExecutorFactoryClass(getThreadPoolFactoryClass());
    }
    tng.setOutputDirectory(getOutputDirectory());

    List<Class<?>> testClasses = getTestClass();
    if (testClasses != null) {
      tng.setTestClasses(testClasses.toArray(new Class[0]));
    }

    tng.setOutputDirectory(getOutputDirectory());

    if (getTestNames() != null) {
      tng.setTestNames(getTestNames());
    }

    if (useDefaultListeners() != null) {
      tng.setUseDefaultListeners(useDefaultListeners());
    }

    tng.setGroups(getGroups());
    tng.setExcludedGroups(getExcludedGroups());
    tng.setTestJar(getTestJar());
    tng.setXmlPathInJar(getXmlPathInJar());
    tng.setJUnit(isJUnit());
    tng.setMixed(isMixed());
    tng.setSkipFailedInvocationCounts(skipFailedInvocationCounts());
    tng.toggleFailureIfAllTestsWereSkipped(failIfAllTestsSkipped());
    tng.setListenersToSkipFromBeingWiredInViaServiceLoaders(getSpiListenersToSkip());

    if (overrideIncludedMethods() != null) {
      tng.setOverrideIncludedMethods(overrideIncludedMethods());
    }

    if (getParallelMode() != null) {
      tng.setParallel(getParallelMode());
    }
    if (getConfigFailurePolicy() != null) {
      tng.setConfigFailurePolicy(getConfigFailurePolicy());
    }
    if (getThreadCount() != null) {
      tng.setThreadCount(getThreadCount());
    }
    if (getDataProviderThreadCount() != null) {
      tng.setDataProviderThreadCount(getDataProviderThreadCount());
    }
    if (getSuiteName() != null) {
      tng.setDefaultSuiteName(getSuiteName());
    }
    if (getTestName() != null) {
      tng.setDefaultTestName(getTestName());
    }
    if (getListener() != null) {
      tng.setListenerClasses(getListener());
    }

    if (getMethodSelectors() != null) {
      for (XmlMethodSelector selector : getMethodSelectors()) {
        tng.addMethodSelector(selector);
      }
    }

    if (getObjectFactory() != null) {
      tng.setObjectFactory(getObjectFactory());
    }
    if (getTestRunnerFactory() != null) {
      tng.setTestRunnerFactoryClass(getTestRunnerFactory());
    }

    if (getReporter() != null) {
      tng.addReporter(getReporter());
    }

    if (!getCommandLineMethods().isEmpty()) {
      tng.setCommandLineMethods(getCommandLineMethods());
    }

    if (getSuiteFiles() != null) {
      tng.setTestSuites(getSuiteFiles());
    }

    tng.setSuiteThreadPoolSize(getSuiteThreadPoolSize());
    tng.setRandomizeSuites(isRandomizeSuites());
    tng.alwaysRunListeners(alwaysRunListeners());
  }

  class ParameterException extends Exception {

    ParameterException(String message) {
      super(message);
    }

    ParameterException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
