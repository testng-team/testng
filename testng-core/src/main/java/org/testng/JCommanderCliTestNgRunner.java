package org.testng;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class JCommanderCliTestNgRunner implements CliTestNgRunner {

  private JCommander jCommander;

  @Override
  public CommandLineArgs parse(String[] argv) throws CommandLineArgs.ParameterException {
    CommandLineArgs cla = new JCommanderCommandLineArgs();

    try {
      jCommander = new JCommander(cla);
      jCommander.parse(argv);
    } catch (ParameterException ex) {
      throw new CommandLineArgs.ParameterException(ex.getMessage(), ex);
    }

    return cla;
  }

  @Override
  public void usage() {
    if (jCommander == null) {
      jCommander = new JCommander(new JCommanderCommandLineArgs());
    }
    jCommander.usage();
  }

  @Override
  public void clear() {
    jCommander = null;
  }
}
