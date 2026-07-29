package org.testng.cli.jcommander;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ServiceLoader;
import org.testng.ITestNGCliRunner;
import org.testng.annotations.Test;
import org.testng.cli.CliOptions;
import org.testng.cli.CliParseException;
import org.testng.xml.XmlSuite;

public class JCommanderCliRunnerTest {

  private static CliOptions parse(String... argv) {
    return new JCommanderCliRunner().parse(argv);
  }

  @Test
  public void serviceIsDiscoverable() {
    ServiceLoader<ITestNGCliRunner> loader =
        ServiceLoader.load(ITestNGCliRunner.class, ITestNGCliRunner.class.getClassLoader());
    assertThat(loader).hasAtLeastOneElementOfType(JCommanderCliRunner.class);
  }

  @Test
  public void suiteFilesArePositional() {
    assertThat(parse("a.xml", "b.xml").suiteFiles).containsExactly("a.xml", "b.xml");
  }

  @Test
  public void verboseAcceptsBothAliases() {
    assertThat(parse("-log", "3", "a.xml").verbose).isEqualTo(3);
    assertThat(parse("-verbose", "4", "a.xml").verbose).isEqualTo(4);
  }

  @Test
  public void testRunnerFactoryAcceptsBothSpellings() {
    assertThat(parse("-testrunfactory", "com.acme.F", "a.xml").testRunnerFactory)
        .isEqualTo("com.acme.F");
    assertThat(parse("-testRunFactory", "com.acme.F", "a.xml").testRunnerFactory)
        .isEqualTo("com.acme.F");
  }

  @Test
  public void parallelModeIsConvertedToAnEnum() {
    assertThat(parse("-parallel", "methods", "a.xml").parallelMode)
        .isEqualTo(XmlSuite.ParallelMode.METHODS);
  }

  /**
   * An unknown value must fail loudly. {@code XmlSuite.ParallelMode.getValidParallel} would
   * silently degrade it to {@code NONE}, which would override what the suite XML declares.
   */
  @Test
  public void unknownParallelModeIsRejected() {
    assertThatThrownBy(() -> parse("-parallel", "bogus", "a.xml"))
        .isInstanceOf(CliParseException.class);
  }

  /**
   * Long standing behaviour: because the suite files are the main parameter, an unknown option is
   * swallowed as a suite file name rather than rejected. Pinned here so that swapping the parser
   * would surface the change.
   */
  @Test
  public void unknownOptionIsTreatedAsASuiteFile() {
    assertThat(parse("-nosuchoption", "a.xml").suiteFiles)
        .containsExactly("-nosuchoption", "a.xml");
  }

  @Test
  public void missingValueForAKnownOptionIsRejected() {
    assertThatThrownBy(() -> parse("-threadcount")).isInstanceOf(CliParseException.class);
  }

  @Test
  public void nonNumericValueForAKnownOptionIsRejected() {
    assertThatThrownBy(() -> parse("-threadcount", "many", "a.xml"))
        .isInstanceOf(CliParseException.class);
  }

  /** {@code -usedefaultlisteners} keeps an arity of one, hence a String rather than a Boolean. */
  @Test
  public void useDefaultListenersHasArityOne() {
    assertThat(parse("a.xml").useDefaultListeners).isEqualTo("true");
    assertThat(parse("-usedefaultlisteners", "false", "a.xml").useDefaultListeners)
        .isEqualTo("false");
  }

  @Test
  public void defaultsAreCarriedOver() {
    CliOptions cli = parse("a.xml");

    assertThat(cli.spiListenersToSkip).isEmpty();
    assertThat(cli.xmlPathInJar).isEqualTo(CliOptions.XML_PATH_IN_JAR_DEFAULT);
    assertThat(cli.suiteThreadPoolSize).isEqualTo(CliOptions.SUITE_THREAD_POOL_SIZE_DEFAULT);
    assertThat(cli.alwaysRunListeners).isTrue();
    assertThat(cli.randomizeSuites).isFalse();
    assertThat(cli.commandLineMethods).isEmpty();
  }

  @Test
  public void everyValueMakesItThroughTheMapping() {
    CliOptions cli =
        parse(
            "-d",
            "target/out",
            "-groups",
            "fast",
            "-excludegroups",
            "slow",
            "-listener",
            "com.acme.L1;com.acme.L2",
            "-listenercomparator",
            "com.acme.C",
            "-methodselectors",
            "com.acme.S:4",
            "-objectfactory",
            "com.acme.OF",
            "-configfailurepolicy",
            "continue",
            "-threadcount",
            "7",
            "-dataproviderthreadcount",
            "9",
            "-suitename",
            "aSuite",
            "-testname",
            "aTest",
            "-reporter",
            "com.acme.R:fileName=out.html",
            "-testclass",
            "com.acme.A,com.acme.B",
            "-testnames",
            "t1,t2",
            "-ignoreMissedTestNames",
            "-testjar",
            "tests.jar",
            "-xmlpathinjar",
            "suites/all.xml",
            "-listenerfactory",
            "com.acme.LF",
            "-methods",
            "com.acme.A.m1",
            "-suitethreadpoolsize",
            "3",
            "-threadpoolfactoryclass",
            "com.acme.TP",
            "-dependencyinjectorfactory",
            "com.acme.DI",
            "-spilistenerstoskip",
            "com.acme.Skipped",
            // The boolean flags matter most here: their CliOptions defaults equal their
            // JCommanderOptions defaults, so a forgotten line in toCliOptions() stays invisible
            // unless they are asserted in a non-default state.
            "-skipfailedinvocationcounts",
            "-randomizesuites",
            "-failwheneverythingskipped",
            "-overrideincludedmethods",
            "-includeAllDataDrivenTestsWhenSkipping",
            "-propagateDataProviderFailureAsTestFailure",
            "-generateResultsPerSuite",
            "-shareThreadPoolForDataProviders",
            "-useGlobalThreadPool",
            "a.xml");

    assertThat(cli.outputDirectory).isEqualTo("target/out");
    assertThat(cli.groups).isEqualTo("fast");
    assertThat(cli.excludedGroups).isEqualTo("slow");
    assertThat(cli.listener).isEqualTo("com.acme.L1;com.acme.L2");
    assertThat(cli.listenerComparator).isEqualTo("com.acme.C");
    assertThat(cli.methodSelectors).isEqualTo("com.acme.S:4");
    assertThat(cli.objectFactory).isEqualTo("com.acme.OF");
    assertThat(cli.configFailurePolicy).isEqualTo("continue");
    assertThat(cli.threadCount).isEqualTo(7);
    assertThat(cli.dataProviderThreadCount).isEqualTo(9);
    assertThat(cli.suiteName).isEqualTo("aSuite");
    assertThat(cli.testName).isEqualTo("aTest");
    assertThat(cli.reporter).isEqualTo("com.acme.R:fileName=out.html");
    assertThat(cli.testClass).isEqualTo("com.acme.A,com.acme.B");
    assertThat(cli.testNames).isEqualTo("t1,t2");
    assertThat(cli.ignoreMissedTestNames).isTrue();
    assertThat(cli.testJar).isEqualTo("tests.jar");
    assertThat(cli.xmlPathInJar).isEqualTo("suites/all.xml");
    assertThat(cli.listenerFactory).isEqualTo("com.acme.LF");
    assertThat(cli.commandLineMethods).containsExactly("com.acme.A.m1");
    assertThat(cli.suiteThreadPoolSize).isEqualTo(3);
    assertThat(cli.threadPoolFactoryClass).isEqualTo("com.acme.TP");
    assertThat(cli.dependencyInjectorFactoryClass).isEqualTo("com.acme.DI");
    assertThat(cli.spiListenersToSkip).isEqualTo("com.acme.Skipped");
    assertThat(cli.suiteFiles).containsExactly("a.xml");
    assertThat(cli.skipFailedInvocationCounts).isTrue();
    assertThat(cli.randomizeSuites).isTrue();
    // -alwaysrunlisteners is an arity-0 flag whose default is already TRUE, so the command line
    // can never observe it in a non-default state, nor switch it off. Left as is for parity.
    assertThat(cli.alwaysRunListeners).isTrue();
    assertThat(cli.failIfAllTestsSkipped).isTrue();
    assertThat(cli.overrideIncludedMethods).isTrue();
    assertThat(cli.includeAllDataDrivenTestsWhenSkipping).isTrue();
    assertThat(cli.propagateDataProviderFailureAsTestFailure).isTrue();
    assertThat(cli.generateResultsPerSuite).isTrue();
    assertThat(cli.shareThreadPoolForDataProviders).isTrue();
    assertThat(cli.useGlobalThreadPool).isTrue();
  }
}
