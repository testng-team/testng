package org.testng;

import java.util.List;
import java.util.Map;
import org.testng.internal.ReporterConfig;
import org.testng.xml.XmlSuite;

public class SurefireCommandLineArgs extends AbstractCommandLineArgs {

  private final Map<String, Object> cmdLineArgs;

  public SurefireCommandLineArgs(Map<String, Object> cmdLineArgs) {
    this.cmdLineArgs = cmdLineArgs;
  }

  @Override
  public List<String> getSuiteFiles() {
    return null;
  }

  @Override
  public Integer getVerbose() {
    int value = parseInt(cmdLineArgs.get(CommandLineArgs.LOG));
    if (value != -1) {
      return value;
    }

    return null;
  }

  @Override
  public String getGroups() {
    return (String) cmdLineArgs.get(CommandLineArgs.GROUPS);
  }

  @Override
  public String getExcludedGroups() {
    return (String) cmdLineArgs.get(CommandLineArgs.EXCLUDED_GROUPS);
  }

  @Override
  public String getOutputDirectory() {
    return (String) cmdLineArgs.get(CommandLineArgs.OUTPUT_DIRECTORY);
  }

  @Override
  public Boolean isJUnit() {
    return (Boolean) cmdLineArgs.get(CommandLineArgs.JUNIT);
  }

  @Override
  public Boolean isMixed() {
    return (Boolean) cmdLineArgs.get(CommandLineArgs.MIXED);
  }

  @Override
  protected String[] getListenerValues() {
    Object listeners = cmdLineArgs.get(CommandLineArgs.LISTENER);
    if (listeners == null) {
      return null;
    }
    if (listeners instanceof List) {
      return ((List<String>) listeners).toArray(new String[0]);
    } else {
      return ((String) listeners).split(",");
    }
  }

  @Override
  protected String getMethodSelectorsValue() {
    return (String) cmdLineArgs.get(CommandLineArgs.METHOD_SELECTORS);
  }

  @Override
  protected String getObjectFactoryValue() {
    return (String) cmdLineArgs.get(CommandLineArgs.OBJECT_FACTORY);
  }

  @Override
  public XmlSuite.ParallelMode getParallelMode() {
    String parallelMode = (String) cmdLineArgs.get(CommandLineArgs.PARALLEL);
    return XmlSuite.ParallelMode.getValidParallel(parallelMode);
  }

  @Override
  public XmlSuite.FailurePolicy getConfigFailurePolicy() {
    String configFailurePolicy = (String) cmdLineArgs.get(CommandLineArgs.CONFIG_FAILURE_POLICY);
    return XmlSuite.FailurePolicy.getValidPolicy(configFailurePolicy);
  }

  @Override
  public Integer getThreadCount() {
    int value = parseInt(cmdLineArgs.get(CommandLineArgs.THREAD_COUNT));
    if (value != -1) {
      return value;
    }

    return null;
  }

  @Override
  public Integer getDataProviderThreadCount() {
    // Not supported by Surefire yet
    int value = parseInt(cmdLineArgs.get(CommandLineArgs.DATA_PROVIDER_THREAD_COUNT));
    if (value != -1) {
      return value;
    }

    return null;
  }

  @Override
  public String getSuiteName() {
    return (String) cmdLineArgs.get(CommandLineArgs.SUITE_NAME);
  }

  @Override
  public String getTestName() {
    return (String) cmdLineArgs.get(CommandLineArgs.TEST_NAME);
  }

  @Override
  public ReporterConfig getReporter() {
    String reporter = (String) cmdLineArgs.get(CommandLineArgs.REPORTER);
    return ReporterConfig.deserialize(reporter);
  }

  @Override
  public Boolean useDefaultListeners() {
    String useDefaultListeners = (String) cmdLineArgs.get(CommandLineArgs.USE_DEFAULT_LISTENERS);
    if (useDefaultListeners == null) {
      return null;
    }
    return Boolean.valueOf(useDefaultListeners);
  }

  @Override
  public Boolean skipFailedInvocationCounts() {
    return (Boolean) cmdLineArgs.get(CommandLineArgs.SKIP_FAILED_INVOCATION_COUNTS);
  }

  @Override
  protected String getTestClassValue() {
    return (String) cmdLineArgs.get(CommandLineArgs.TEST_CLASS);
  }

  @Override
  protected String getTestNamesValue() {
    return (String) cmdLineArgs.get(CommandLineArgs.TEST_NAMES);
  }

  @Override
  public String getTestJar() {
    return (String) cmdLineArgs.get(CommandLineArgs.TEST_JAR);
  }

  @Override
  public String getXmlPathInJar() {
    return (String) cmdLineArgs.get(CommandLineArgs.XML_PATH_IN_JAR);
  }

  @Override
  protected String getTestRunnerFactoryValue() {
    return (String) cmdLineArgs.get(CommandLineArgs.TEST_RUNNER_FACTORY);
  }

  @Override
  public Integer getPort() {
    return null;
  }

  @Override
  public String getHost() {
    return null;
  }

  @Override
  public List<String> getCommandLineMethods() {
    return null;
  }

  @Override
  public Integer getSuiteThreadPoolSize() {
    int value = parseInt(cmdLineArgs.get(CommandLineArgs.SUITE_THREAD_POOL_SIZE));
    if (value != -1) {
      return value;
    }

    return null;
  }

  @Override
  public Boolean isRandomizeSuites() {
    return null;
  }

  @Override
  public Boolean isDebug() {
    return null;
  }

  @Override
  public Boolean alwaysRunListeners() {
    return null;
  }

  @Override
  public String getThreadPoolFactoryClass() {
    return (String) cmdLineArgs.get(CommandLineArgs.DEPENDENCY_INJECTOR_FACTORY);
  }

  @Override
  public Class<IInjectorFactory> getDependencyInjectorFactory() {
    return null;
  }

  @Override
  public Boolean failIfAllTestsSkipped() {
    return Boolean.parseBoolean(
        cmdLineArgs
            .getOrDefault(CommandLineArgs.FAIL_IF_ALL_TESTS_SKIPPED, Boolean.FALSE)
            .toString());
  }

  @Override
  protected String getSpiListenersToSkipValue() {
    return (String) cmdLineArgs.getOrDefault(CommandLineArgs.LISTENERS_TO_SKIP_VIA_SPI, "");
  }

  @Override
  public Boolean overrideIncludedMethods() {
    return null;
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
}
