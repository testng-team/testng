package org.testng.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Arrays;
import java.util.Collections;
import org.testng.CommandLineArgs;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CliConfigurerValidateTest {

  @Test
  public void nothingToRunIsRejected() {
    assertThatThrownBy(() -> CliConfigurer.validate(new CliOptions()))
        .isInstanceOf(CliParseException.class)
        .hasMessageContaining(
            "You need to specify at least one testng.xml, one class or one method");
  }

  @Test
  public void groupsWithoutTestClassIsRejected() {
    CliOptions cli = new CliOptions();
    cli.commandLineMethods = Collections.singletonList("com.acme.Sample.aMethod");
    cli.groups = "fast";

    assertThatThrownBy(() -> CliConfigurer.validate(cli))
        .isInstanceOf(CliParseException.class)
        .hasMessageContaining("Groups option should be used with testclass option");
  }

  @Test
  public void groupsWithTestClassIsAccepted() {
    CliOptions cli = new CliOptions();
    cli.testClass = "com.acme.Sample";
    cli.groups = "fast";

    assertThatCode(() -> CliConfigurer.validate(cli)).doesNotThrowAnyException();
  }

  @Test
  public void suiteFilesAloneAreAccepted() {
    CliOptions cli = new CliOptions();
    cli.suiteFiles = Arrays.asList("testng.xml");

    assertThatCode(() -> CliConfigurer.validate(cli)).doesNotThrowAnyException();
  }

  @DataProvider
  public Object[][] commandLines() {
    return new Object[][] {
      {null, null, null, null, Collections.<String>emptyList()},
      {"com.acme.Sample", null, null, null, Collections.<String>emptyList()},
      {null, "tests.jar", null, null, Collections.<String>emptyList()},
      {null, null, null, null, Collections.singletonList("com.acme.Sample.aMethod")},
      {null, null, "fast", null, Collections.singletonList("com.acme.Sample.aMethod")},
      {null, null, null, "slow", Collections.singletonList("com.acme.Sample.aMethod")},
      {"com.acme.Sample", null, "fast", null, Collections.<String>emptyList()},
      {null, "tests.jar", "fast", null, Collections.<String>emptyList()},
    };
  }

  /**
   * The frozen {@code TestNG.validateCommandLineParameters} must keep accepting and rejecting
   * exactly what {@link CliConfigurer#validate} does, for as long as both exist.
   */
  @Test(dataProvider = "commandLines")
  public void validationMatchesTheDeprecatedTestNgEntryPoint(
      String testClass,
      String testJar,
      String groups,
      String excludedGroups,
      java.util.List<String> methods) {
    CliOptions cli = new CliOptions();
    cli.testClass = testClass;
    cli.testJar = testJar;
    cli.groups = groups;
    cli.excludedGroups = excludedGroups;
    cli.commandLineMethods = methods;

    @SuppressWarnings("deprecation")
    CommandLineArgs args = new CommandLineArgs();
    args.testClass = testClass;
    args.testJar = testJar;
    args.groups = groups;
    args.excludedGroups = excludedGroups;
    args.commandLineMethods = methods;

    Throwable fromCli = catchThrowable(() -> CliConfigurer.validate(cli));
    Throwable fromTestNg = catchThrowable(() -> LegacyValidator.validate(args));

    assertThat(fromCli == null).isEqualTo(fromTestNg == null);
    if (fromCli != null) {
      assertThat(fromCli.getMessage()).isEqualTo(fromTestNg.getMessage());
    }
  }

  /** Gives access to the {@code protected static} validation kept on {@link TestNG}. */
  private static final class LegacyValidator extends TestNG {
    @SuppressWarnings("deprecation")
    static void validate(CommandLineArgs args) {
      validateCommandLineParameters(args);
    }
  }
}
