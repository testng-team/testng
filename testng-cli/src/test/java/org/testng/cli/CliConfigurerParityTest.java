package org.testng.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.testng.CommandLineArgs;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

/**
 * {@link CliConfigurer#configure} and the frozen {@code TestNG#configure(CommandLineArgs)} must
 * apply a command line identically for as long as both exist. Nothing else enforces that, so this
 * test configures two instances from equivalent inputs and compares the resulting state field by
 * field.
 */
public class CliConfigurerParityTest {

  /**
   * The only fields that cannot be value-compared: each instance builds its own object and none of
   * these types overrides equals. Everything else is either a scalar or an empty collection on both
   * sides, and must therefore match.
   */
  private static final List<String> IDENTITY_NOT_VALUE =
      Arrays.asList(
          "m_configuration",
          "m_objectFactory",
          "exitCodeListener",
          "m_annotationTransformer",
          "m_defaultAnnoProcessor");

  @Test
  public void configuringViaCliOptionsMatchesTheFrozenCommandLineArgsPath() throws Exception {
    TestNG viaCli = new TestNG();
    CliConfigurer.configure(viaCli, populatedCliOptions());

    FrozenConfigurer viaFrozen = new FrozenConfigurer();
    viaFrozen.apply(populatedCommandLineArgs());

    Map<String, Object> left = configurationOf(viaCli);
    Map<String, Object> right = configurationOf(viaFrozen);

    assertThat(left)
        .as("state produced by CliConfigurer vs the frozen TestNG.configure(CommandLineArgs)")
        .isEqualTo(right);
    // Guard against the comparison silently degenerating to an empty map.
    assertThat(left).hasSizeGreaterThan(20);
  }

  private static CliOptions populatedCliOptions() {
    CliOptions cli = new CliOptions();
    cli.suiteFiles = Arrays.asList("a.xml", "b.xml");
    cli.verbose = 3;
    cli.groups = "fast";
    cli.excludedGroups = "slow";
    cli.outputDirectory = "target/parity";
    cli.parallelMode = XmlSuite.ParallelMode.METHODS;
    cli.configFailurePolicy = "continue";
    cli.threadCount = 7;
    cli.dataProviderThreadCount = 9;
    cli.suiteName = "aSuite";
    cli.testName = "aTest";
    cli.useDefaultListeners = "false";
    cli.skipFailedInvocationCounts = Boolean.TRUE;
    cli.testNames = "t1,t2";
    cli.ignoreMissedTestNames = true;
    cli.testJar = "tests.jar";
    cli.xmlPathInJar = "suites/all.xml";
    cli.commandLineMethods = Arrays.asList("com.acme.A.m1", "com.acme.A.m2");
    cli.suiteThreadPoolSize = 3;
    cli.randomizeSuites = Boolean.TRUE;
    cli.alwaysRunListeners = Boolean.FALSE;
    cli.failIfAllTestsSkipped = Boolean.TRUE;
    cli.spiListenersToSkip = "com.acme.Skipped";
    cli.overrideIncludedMethods = Boolean.TRUE;
    cli.includeAllDataDrivenTestsWhenSkipping = Boolean.TRUE;
    cli.generateResultsPerSuite = Boolean.TRUE;
    cli.shareThreadPoolForDataProviders = Boolean.TRUE;
    cli.useGlobalThreadPool = Boolean.TRUE;
    return cli;
  }

  /**
   * Copies the populated options into the deprecated bag by field name. Reflective on purpose: a
   * hand-written copy that silently misses a field would weaken the comparison in the permissive
   * direction. {@code CliOptionNamesTest} already pins that the two classes agree.
   */
  @SuppressWarnings("deprecation")
  private static CommandLineArgs populatedCommandLineArgs() throws IllegalAccessException {
    CommandLineArgs args = new CommandLineArgs();
    CliOptions cli = populatedCliOptions();
    for (Field source : CliOptions.class.getDeclaredFields()) {
      if (java.lang.reflect.Modifier.isStatic(source.getModifiers()) || source.isSynthetic()) {
        continue;
      }
      Field target;
      try {
        target = CommandLineArgs.class.getDeclaredField(source.getName());
      } catch (NoSuchFieldException e) {
        throw new AssertionError("CommandLineArgs has no " + source.getName(), e);
      }
      target.set(args, source.get(cli));
    }
    return args;
  }

  /** Reads every comparable instance field of {@link TestNG}, including inherited ones. */
  private static Map<String, Object> configurationOf(TestNG testng) throws IllegalAccessException {
    Map<String, Object> state = new LinkedHashMap<>();
    for (Field field : TestNG.class.getDeclaredFields()) {
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
          || field.isSynthetic()
          || IDENTITY_NOT_VALUE.contains(field.getName())) {
        continue;
      }
      field.setAccessible(true);
      Object value = field.get(testng);
      state.put(
          field.getName(), value instanceof Object[] ? Arrays.asList((Object[]) value) : value);
    }
    return state;
  }

  /** Gives access to the {@code protected} frozen configuration path kept on {@link TestNG}. */
  private static final class FrozenConfigurer extends TestNG {
    @SuppressWarnings("deprecation")
    void apply(CommandLineArgs args) {
      configure(args);
    }
  }
}
