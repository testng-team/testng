package org.testng.cli.jcommander;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.testng.cli.AbstractCliRunner;
import org.testng.cli.CliOptions;
import org.testng.cli.CliParseException;

/**
 * The JCommander backed implementation of {@link org.testng.ITestNGCliRunner}, wired in through
 * {@code META-INF/services}.
 *
 * @since 7.13
 */
public class JCommanderCliRunner extends AbstractCliRunner {

  @Override
  protected CliOptions parse(String[] argv) {
    JCommanderOptions options = new JCommanderOptions();
    try {
      new JCommander(options).parse(argv);
    } catch (ParameterException ex) {
      throw new CliParseException(ex.getMessage(), ex);
    }
    return options.toCliOptions();
  }

  @Override
  public void usage() {
    new JCommander(new JCommanderOptions()).usage();
  }
}
