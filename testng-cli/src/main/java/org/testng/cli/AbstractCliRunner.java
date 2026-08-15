package org.testng.cli;

import org.jspecify.annotations.Nullable;
import org.testng.ITestListener;
import org.testng.ITestNGCliRunner;
import org.testng.TestNG;
import org.testng.TestNGException;

/**
 * Base class for command line front ends. Subclasses only have to turn {@code argv} into {@link
 * CliOptions} and print a usage banner; validation, configuration and the run itself are shared.
 *
 * <p>Nothing here terminates the JVM: a command line that cannot be honoured comes back as a {@link
 * CliParseException}, which lets the same code drive a real command line, a test, or an embedding
 * process.
 *
 * @since 7.13
 */
public abstract class AbstractCliRunner implements ITestNGCliRunner {

  /**
   * Parses the raw command line. This is the only thing a front end has to supply.
   *
   * @param argv the TestNG command line parameters.
   * @return the parsed options.
   * @throws CliParseException when {@code argv} is not a valid command line.
   */
  protected abstract CliOptions parse(String[] argv);

  @Override
  public TestNG run(String[] argv, @Nullable ITestListener listener) {
    TestNG result = new TestNG();

    if (null != listener) {
      result.addListener(listener);
    }

    CliOptions cli = parse(argv);
    CliConfigurer.validate(cli);
    CliConfigurer.configure(result, cli);

    try {
      result.run();
    } catch (TestNGException ex) {
      // A command line reports a broken run as a failing status, not as a propagating exception.
      result.reportRunFailure(ex);
    }

    return result;
  }
}
