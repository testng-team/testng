package org.testng;

import com.beust.jcommander.Parameter;

public class CommandLineArgs {

  @Parameter(name = "-port", description = "The port")
  private Integer m_port;

  @Parameter(name = "-host", description = "The host")
  private String m_host;

  @Parameter(name = "-master", description ="Master mode")
  private Boolean m_master;

  @Parameter(name = "-slave", description ="Slave mode")
  private Boolean m_slave;

  @Parameter(name = "-groups", description = "Comma-separated list of group names to be run")
  private String m_groups;

  @Parameter(name = "-excludedgroups", description ="Comma-separated list of group names to be run")
  private String m_excludedGroups;
  
  @Parameter(name = "-d", description ="Output directory")
  private String m_outputDirectory;
  
  @Parameter(name = "-junit", description ="JUnit mode")
  private Boolean m_junit;

  @Parameter(name = "-listener", description = "List of .class files or list of class names" +
      " implementing ITestListener or ISuiteListener")
  private String m_listener;

  @Parameter(name = "-methodselectors", description = "List of .class files or list of class " +
  		"names implementing IMethodSelector")
  private String m_methodSelectors;

  @Parameter(name = "-objectfactory", description = "List of .class files or list of class names " +
      "implementing ITestRunnerFactory")
  private String m_objectFactory;

  @Parameter(name = "-parallel", description = "Parallel mode (methods, tests or classes)")
  private String m_parallelMode;

  @Parameter(name = "-threadcount", description = "Number of threads to use when running tests " +
      "in parallel")
  private Integer m_threadCount;

  @Parameter(name = "-dataproviderthreadcount", description = "Number of threads to use when " +
      "running tests in parallel")
  private Integer m_dataProviderThreadCount;

  @Parameter(name = "-suitename", description = "Default name of test suite, if not specified " +
      "in suite definition file or source code")
  private String m_suiteName;

  @Parameter(name = "-testname", description = "Default name of test, if not specified in suite" +
      "definition file or source code")
  private String m_testName;

  @Parameter(name = "-reporter", description = "Extended configuration for custom report listener")
  private String m_reporter;

  /**
   * Used as map key for the complete list of report listeners provided with the above argument
   */
  @Parameter(name = "-reporterslist")
  private String m_reportersList;

  @Parameter(name = "-skipfailedinvocationcounts")
  private Boolean m_skipFailedInvocationCounts;

  @Parameter(name = "-testclass", description = "The list of test classes")
  private String m_testClass;

  @Parameter(name = "-testnames", description = "The list of test names to run")
  private String m_testNames;

  @Parameter(name = "-testjar", description = "")
  private String m_testJar;

  @Parameter(name = "-testRunFactory", description = "")
  private String m_testRunFactory;

  public static final String DATA_PROVIDER_THREAD_COUNT = "-dataproviderthreadcount";
}
