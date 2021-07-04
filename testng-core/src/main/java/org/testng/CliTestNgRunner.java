package org.testng;

import org.testng.internal.ExitCode;
import org.testng.log4testng.Logger;

public interface CliTestNgRunner {

  CommandLineArgs parse(String[] argv) throws CommandLineArgs.ParameterException;

  void usage();

  void clear();

  default void exitWithError(String msg) {
    System.err.println(msg);
    usage();
    System.exit(1);
  }

  class Main {

    private static final Logger LOGGER = Logger.getLogger(TestNG.class);

    /**
     * The TestNG entry point for command line execution.
     *
     * @param argv the TestNG command line parameters.
     */
    public static void main(String[] argv) {
      CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
      TestNG testng = privateMain(cliRunner, argv, null);
      System.exit(testng.getStatus());
    }

    /**
     * <B>Note</B>: this method is not part of the public API and is meant for internal usage only.
     *
     * @param argv The param arguments
     * @param listener The listener
     * @return The TestNG instance
     */
    public static TestNG privateMain(
        CliTestNgRunner cliRunner, String[] argv, ITestListener listener) {
      TestNG result = new TestNG();

      if (null != listener) {
        result.addListener(listener);
      }

      // Parse the arguments
      try {
        CommandLineArgs cla = cliRunner.parse(argv);
        validateCommandLineParameters(cla);
        cla.configure(result);
      } catch (CommandLineArgs.ParameterException ex) {
        cliRunner.exitWithError(ex.getMessage());
      }

      // Run
      try {
        result.run();
      } catch (TestNGException ex) {
        if (TestRunner.getVerbose() > 1) {
          ex.printStackTrace(System.out);
        } else {
          LOGGER.error(ex.getMessage());
        }
        result.setExitCode(ExitCode.newExitCodeRepresentingFailure());
      }

      return result;
    }

    /**
     * Double check that the command line parameters are valid.
     *
     * @param args The command line to check
     */
    protected static void validateCommandLineParameters(CommandLineArgs args)
        throws CommandLineArgs.ParameterException {
      args.validate();
    }
  }
}
