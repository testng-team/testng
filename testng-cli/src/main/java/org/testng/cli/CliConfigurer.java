package org.testng.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.testng.IExecutorServiceFactory;
import org.testng.IInjectorFactory;
import org.testng.ITestNGListener;
import org.testng.ITestNGListenerFactory;
import org.testng.ITestObjectFactory;
import org.testng.ITestRunnerFactory;
import org.testng.ListenerComparator;
import org.testng.TestNG;
import org.testng.internal.ClassHelper;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;

/**
 * Turns the values a parser collected into {@link CliOptions} into a configured {@link TestNG}
 * instance. This is where every "string to object" conversion a command line needs lives: class
 * loading, listener wiring, method selector parsing and reporter configuration.
 *
 * <p>The deprecated {@code TestNG#configure(org.testng.CommandLineArgs)} keeps a frozen copy of
 * this behaviour for the benefit of subclasses such as {@code RemoteTestNG}; both must stay in sync
 * until that method is removed.
 *
 * @since 7.13
 */
public final class CliConfigurer {

  // Deliberately keyed on TestNG so that the message reads the same as before the CLI extraction.
  private static final Logger LOGGER = Logger.getLogger(TestNG.class);

  private static final String BAD_METHOD_SELECTOR =
      "Method selector value was not in the format org.example.Selector:4";

  private CliConfigurer() {}

  /**
   * Narrows a class loaded by name without checking it, so that an unsuitable {@code
   * -objectfactory} or {@code -testrunfactory} fails where it did before this logic left {@code
   * testng-core}: when the instance is created, not here.
   */
  @SuppressWarnings("unchecked")
  private static <T> Class<? extends T> uncheckedSubclass(Class<?> clazz) {
    return (Class<? extends T>) clazz;
  }

  /**
   * Double checks that the command line parameters are consistent.
   *
   * @param cli the parsed command line.
   * @throws CliParseException when the combination of options cannot select anything to run.
   */
  public static void validate(CliOptions cli) {
    String testClasses = cli.testClass;
    List<String> testNgXml = cli.suiteFiles;
    String testJar = cli.testJar;
    List<String> methods = cli.commandLineMethods;

    if (testClasses == null
        && testJar == null
        && (testNgXml == null || testNgXml.isEmpty())
        && (methods == null || methods.isEmpty())) {
      throw new CliParseException(
          "You need to specify at least one testng.xml, one class or one method");
    }

    String groups = cli.groups;
    String excludedGroups = cli.excludedGroups;

    if (testJar == null
        && (null != groups || null != excludedGroups)
        && testClasses == null
        && (testNgXml == null || testNgXml.isEmpty())) {
      throw new CliParseException("Groups option should be used with testclass option");
    }
  }

  /**
   * Applies the parsed command line onto a {@link TestNG} instance.
   *
   * @param testng the instance to configure.
   * @param cli the parsed command line.
   */
  public static void configure(TestNG testng, CliOptions cli) {
    Objects.requireNonNull(
        cli.spiListenersToSkip, CliOptions.LISTENERS_TO_SKIP_VIA_SPI + " must not be null");
    Optional.ofNullable(cli.useGlobalThreadPool).ifPresent(testng::shouldUseGlobalThreadPool);
    Optional.ofNullable(cli.shareThreadPoolForDataProviders)
        .ifPresent(testng::shareThreadPoolForDataProviders);
    // FIXME: the field defaults to FALSE rather than null, so this toggle is switched on for every
    // command line run and -propagateDataProviderFailureAsTestFailure cannot be turned off. Kept
    // verbatim for parity with the frozen TestNG#configure(CommandLineArgs); fixing it is a
    // behaviour change that belongs in its own issue.
    Optional.ofNullable(cli.propagateDataProviderFailureAsTestFailure)
        .ifPresent(value -> testng.propagateDataProviderFailureAsTestFailure());
    testng.setReportAllDataDrivenTestsAsSkipped(cli.includeAllDataDrivenTestsWhenSkipping);

    Optional.ofNullable(cli.listenerFactory)
        .map(ClassHelper::forName)
        .filter(ITestNGListenerFactory.class::isAssignableFrom)
        .map(it -> it.asSubclass(ITestNGListenerFactory.class))
        .ifPresent(testng::setListenerFactoryClass);

    Optional.ofNullable(cli.generateResultsPerSuite).ifPresent(testng::setGenerateResultsPerSuite);

    Optional.ofNullable(cli.listenerComparator)
        .map(ClassHelper::forName)
        .filter(ListenerComparator.class::isAssignableFrom)
        .map(it -> it.asSubclass(ListenerComparator.class))
        .ifPresent(testng::setListenerComparatorClass);

    if (cli.verbose != null) {
      testng.setVerbose(cli.verbose);
    }
    if (cli.dependencyInjectorFactoryClass != null) {
      Class<?> clazz = ClassHelper.forName(cli.dependencyInjectorFactoryClass);
      if (clazz != null && IInjectorFactory.class.isAssignableFrom(clazz)) {
        testng.setInjectorFactoryClass(clazz.asSubclass(IInjectorFactory.class));
      }
    }
    Optional.ofNullable(cli.threadPoolFactoryClass)
        .map(ClassHelper::forName)
        .filter(IExecutorServiceFactory.class::isAssignableFrom)
        .map(it -> it.asSubclass(IExecutorServiceFactory.class))
        .ifPresent(testng::setExecutorServiceFactoryClass);

    testng.setOutputDirectory(cli.outputDirectory);

    String testClasses = cli.testClass;
    if (null != testClasses) {
      String[] strClasses = testClasses.split(",");
      List<Class<?>> classes = new ArrayList<>();
      for (String c : strClasses) {
        classes.add(ClassHelper.fileToClass(c));
      }

      testng.setTestClasses(classes.toArray(new Class[0]));
    }

    if (cli.testNames != null) {
      testng.setTestNames(Arrays.asList(cli.testNames.split(",")));
      testng.setIgnoreMissedTestNames(cli.ignoreMissedTestNames);
    }

    // Note: can't use a Boolean field here because we are allowing a boolean
    // parameter with an arity of 1 ("-usedefaultlisteners false")
    if (cli.useDefaultListeners != null) {
      testng.setUseDefaultListeners("true".equalsIgnoreCase(cli.useDefaultListeners));
    }

    testng.setGroups(cli.groups);
    testng.setExcludedGroups(cli.excludedGroups);
    testng.setTestJar(cli.testJar);
    testng.setXmlPathInJar(cli.xmlPathInJar);
    testng.setSkipFailedInvocationCounts(cli.skipFailedInvocationCounts);
    testng.toggleFailureIfAllTestsWereSkipped(cli.failIfAllTestsSkipped);
    testng.setListenersToSkipFromBeingWiredInViaServiceLoaders(cli.spiListenersToSkip.split(","));

    testng.setOverrideIncludedMethods(cli.overrideIncludedMethods);

    if (cli.parallelMode != null) {
      testng.setParallel(cli.parallelMode);
    }
    if (cli.configFailurePolicy != null) {
      testng.setConfigFailurePolicy(XmlSuite.FailurePolicy.getValidPolicy(cli.configFailurePolicy));
    }
    if (cli.threadCount != null) {
      testng.setThreadCount(cli.threadCount);
    }
    if (cli.dataProviderThreadCount != null) {
      testng.setDataProviderThreadCount(cli.dataProviderThreadCount);
    }
    if (cli.suiteName != null) {
      testng.setDefaultSuiteName(cli.suiteName);
    }
    if (cli.testName != null) {
      testng.setDefaultTestName(cli.testName);
    }
    if (cli.listener != null) {
      String sep = ";";
      if (cli.listener.contains(",")) {
        sep = ",";
      }
      String[] strs = Utils.split(cli.listener, sep);
      List<Class<? extends ITestNGListener>> classes = new ArrayList<>();

      for (String cls : strs) {
        Class<?> clazz = ClassHelper.fileToClass(cls);
        if (ITestNGListener.class.isAssignableFrom(clazz)) {
          classes.add(clazz.asSubclass(ITestNGListener.class));
        }
      }

      testng.setListenerClasses(classes);
    }

    if (null != cli.methodSelectors) {
      String[] strs = Utils.split(cli.methodSelectors, ",");
      for (String cls : strs) {
        String[] sel = Utils.split(cls, ":");
        try {
          if (sel.length == 2) {
            testng.addMethodSelector(sel[0], Integer.parseInt(sel[1]));
          } else {
            LOGGER.error(BAD_METHOD_SELECTOR);
          }
        } catch (NumberFormatException nfe) {
          LOGGER.error(BAD_METHOD_SELECTOR);
        }
      }
    }

    if (cli.objectFactory != null) {
      testng.setObjectFactory(
          CliConfigurer.<ITestObjectFactory>uncheckedSubclass(
              ClassHelper.fileToClass(cli.objectFactory)));
    }
    if (cli.testRunnerFactory != null) {
      testng.setTestRunnerFactoryClass(
          CliConfigurer.<ITestRunnerFactory>uncheckedSubclass(
              ClassHelper.fileToClass(cli.testRunnerFactory)));
    }

    testng.addReporter(cli.reporter);

    if (!cli.commandLineMethods.isEmpty()) {
      testng.setCommandLineMethods(cli.commandLineMethods);
    }

    if (cli.suiteFiles != null) {
      testng.setTestSuites(cli.suiteFiles);
    }

    testng.setSuiteThreadPoolSize(cli.suiteThreadPoolSize);
    testng.setRandomizeSuites(cli.randomizeSuites);
    testng.alwaysRunListeners(cli.alwaysRunListeners);
  }
}
