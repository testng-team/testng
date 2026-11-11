package org.testng.cli;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.testng.CommandLineArgs;
import org.testng.TestNG;
import org.testng.annotations.Test;

/**
 * GITHUB-3565. {@code -propagateDataProviderFailureAsTestFailure} is off unless it is given, on
 * every path that turns command line values into a configured {@link TestNG}.
 */
public class CliConfigurerDataProviderFailureTest {

  @Test(description = "GITHUB-3565")
  public void cliConfigurerLeavesTheSettingOffByDefault() {
    Inspectable testng = new Inspectable();
    CliConfigurer.configure(testng, new CliOptions());
    assertThat(testng.propagates()).isFalse();
  }

  @Test(description = "GITHUB-3565")
  public void cliConfigurerTurnsTheSettingOnWhenGiven() {
    Inspectable testng = new Inspectable();
    CliOptions cli = new CliOptions();
    cli.propagateDataProviderFailureAsTestFailure = Boolean.TRUE;
    CliConfigurer.configure(testng, cli);
    assertThat(testng.propagates()).isTrue();
  }

  @Test(description = "GITHUB-3565")
  public void commandLineArgsLeaveTheSettingOffByDefault() {
    Inspectable testng = new Inspectable();
    testng.apply(new CommandLineArgs());
    assertThat(testng.propagates()).isFalse();
  }

  @Test(description = "GITHUB-3565")
  public void commandLineArgsTurnTheSettingOnWhenGiven() {
    Inspectable testng = new Inspectable();
    CommandLineArgs args = new CommandLineArgs();
    args.propagateDataProviderFailureAsTestFailure = Boolean.TRUE;
    testng.apply(args);
    assertThat(testng.propagates()).isTrue();
  }

  @Test(description = "GITHUB-3565")
  @SuppressWarnings("deprecation")
  public void configureMapLeavesTheSettingOffByDefault() {
    Inspectable testng = new Inspectable();
    testng.configure(new HashMap<>());
    assertThat(testng.propagates()).isFalse();
  }

  @Test(description = "GITHUB-3565")
  @SuppressWarnings("deprecation")
  public void configureMapTurnsTheSettingOnWhenGiven() {
    Inspectable testng = new Inspectable();
    Map<String, Object> values =
        new HashMap<>(
            Collections.singletonMap(
                CommandLineArgs.PROPAGATE_DATA_PROVIDER_FAILURES_AS_TEST_FAILURE, "true"));
    testng.configure(values);
    assertThat(testng.propagates()).isTrue();
  }

  @Test(description = "GITHUB-3565")
  @SuppressWarnings("deprecation")
  public void configureMapLeavesTheSettingOffForANullValue() {
    Inspectable testng = new Inspectable();
    Map<String, Object> values = new HashMap<>();
    values.put(CommandLineArgs.PROPAGATE_DATA_PROVIDER_FAILURES_AS_TEST_FAILURE, null);
    testng.configure(values);
    assertThat(testng.propagates()).isFalse();
  }

  @Test(description = "GITHUB-3565")
  public void cliConfigurerLeavesTheSettingOffWhenFalse() {
    Inspectable testng = new Inspectable();
    CliOptions cli = new CliOptions();
    cli.propagateDataProviderFailureAsTestFailure = Boolean.FALSE;
    CliConfigurer.configure(testng, cli);
    assertThat(testng.propagates()).isFalse();
  }

  @Test(description = "GITHUB-3565")
  public void commandLineArgsLeaveTheSettingOffWhenFalse() {
    Inspectable testng = new Inspectable();
    CommandLineArgs args = new CommandLineArgs();
    args.propagateDataProviderFailureAsTestFailure = Boolean.FALSE;
    testng.apply(args);
    assertThat(testng.propagates()).isFalse();
  }

  @Test(description = "GITHUB-3565")
  @SuppressWarnings("deprecation")
  public void configureMapLeavesTheSettingOffWhenFalse() {
    Inspectable testng = new Inspectable();
    Map<String, Object> values = new HashMap<>();
    values.put(CommandLineArgs.PROPAGATE_DATA_PROVIDER_FAILURES_AS_TEST_FAILURE, "false");
    testng.configure(values);
    assertThat(testng.propagates()).isFalse();
  }

  /** Exposes the protected configuration and the frozen command line path of {@link TestNG}. */
  private static final class Inspectable extends TestNG {
    boolean propagates() {
      return getConfiguration().isPropagateDataProviderFailureAsTestFailure();
    }

    @SuppressWarnings("deprecation")
    void apply(CommandLineArgs args) {
      configure(args);
    }
  }
}
