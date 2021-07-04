package org.testng;

import java.util.List;
import java.util.Map;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

public class SurefireCommandLineArgs implements CommandLineArgs {

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
  public String getListener() {
    Object listeners = cmdLineArgs.get(CommandLineArgs.LISTENER);
    if (listeners instanceof List) {
      return Utils.join((List<?>) listeners, ",");
    } else {
      return (String) listeners;
    }
  }

  @Override
  public String getMethodSelectors() {
    return (String) cmdLineArgs.get(CommandLineArgs.METHOD_SELECTORS);
  }

  @Override
  public String getObjectFactory() {
    return (String) cmdLineArgs.get(CommandLineArgs.OBJECT_FACTORY);
  }

  @Override
  public XmlSuite.ParallelMode getParallelMode() {
    String parallelMode = (String) cmdLineArgs.get(CommandLineArgs.PARALLEL);
    return XmlSuite.ParallelMode.getValidParallel(parallelMode);
  }

  @Override
  public String getConfigFailurePolicy() {
    return (String) cmdLineArgs.get(CommandLineArgs.CONFIG_FAILURE_POLICY);
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
  public String getReporter() {
    return (String) cmdLineArgs.get(CommandLineArgs.REPORTER);
  }

  @Override
  public String useDefaultListeners() {
    return (String) cmdLineArgs.get(CommandLineArgs.USE_DEFAULT_LISTENERS);
  }

  @Override
  public Boolean skipFailedInvocationCounts() {
    return (Boolean) cmdLineArgs.get(CommandLineArgs.SKIP_FAILED_INVOCATION_COUNTS);
  }

  @Override
  public String getTestClass() {
    return (String) cmdLineArgs.get(CommandLineArgs.TEST_CLASS);
  }

  @Override
  public String getTestNames() {
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
  public String getTestRunnerFactory() {
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
  public String getDependencyInjectorFactory() {
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
  public String getSpiListenersToSkip() {
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
