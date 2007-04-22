package org.testng;

import java.util.Map;

/**
 * Represents the various name/value pairs that may be used to configure a
 * testng instance via the {@link TestNG#configureAndRun(java.util.Map)} method.
 *
 * @since 5.6
 */
public interface ITestNGConfiguration {

  String SHOW_TESTNG_STACK_FRAMESTRING = "testng.show.stack.frames";
  String TEST_CLASSPATH = "testng.test.classpath";

  /** The test report output directory option. */
  String OUTPUT_DIRECTORy = "d";

  /** The list of test classes option. */
  String TEST_CLASSES = "testclass";
  String TEST_JAR = "testjar";

  /** The source directory option (when using JavaDoc type annotations). */
  String SOURCE_DIRECTORY = "sourcedir";
  /** The logging level option. */
  String LOG_LEVEL = "log";

  /** The default annotations option (useful in TestNG 15 only). */
  String ANNOTATION_TYPE = "annotations";

  String GROUPS = "groups";
  String EXCLUDED_GROUPS = "excludegroups";
  String TESTRUNNER_FACTORY = "testrunfactory";
  String TEST_LISTENER = "listener";
  String SUITE_LISTENER = "suitelistener";
  String OBJECT_FACTORY = "objectfactory";
  String XML_SUITES = "testng.suite.definitions";
  String JUNIT = "junit";
  String THREAD_COUNT = "threadcount";
  String USE_DEFAULT_LISTENERS = "usedefaultlisteners";
  String PARALLEL_MODE = "parallel";
  String DEFAULT_SUITE_NAME = "suitename";
  String DEFAULT_TEST_NAME = "testname";
  
  String PORT = "port";
  String HOST = "host";
  String SLAVE_PROPERTIES = "slave";
  String MASTER_PROPERTIES = "master";

  /**
   * Used to pass a reporter configuration in the form <code>-reporter <reporter_name_or_class>:option=value[,option=value]</code>
   */
  String REPORTER = "reporter";

  /**
   * Used as map key for the complete list of report listeners provided with the above argument
   */
  String REPORTERS_LIST = "reporterslist";

  /**
   * Parses and sets up all of the properties needed by {@link TestNG}.
   *
   * @param config
   *          The properties in string name/value pairs.
   */
  void load(Map config);

  /**
   * Configures the specified test with whatever configuration properties this parser
   * currently holds.
   *
   * @param test
   *          The {@link TestNG} instance to configure.
   */
  void configure(TestNG test);
}
