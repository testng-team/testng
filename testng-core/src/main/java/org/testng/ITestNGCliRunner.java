package org.testng;

/**
 * Service provider interface backing {@link TestNG#main(String[])}. Implementations are discovered
 * with {@link java.util.ServiceLoader}, which keeps the command line parsing library out of {@code
 * testng-core}.
 *
 * <p>The reference implementation lives in the {@code testng-jcommander} module and is bundled
 * inside the {@code org.testng:testng} jar.
 *
 * <p>Implementations must be embeddable: they never terminate the JVM and never write a usage
 * banner of their own accord. Deciding what a bad command line costs the process belongs to the
 * entry point, which is {@link TestNG#main(String[])}.
 *
 * @since 7.13
 */
public interface ITestNGCliRunner {

  /**
   * Parses the command line, configures a fresh {@link TestNG} instance and runs it.
   *
   * <p>A run that fails is <em>not</em> an error here: the returned instance carries the outcome in
   * {@link TestNG#getStatus()}. Only a command line that cannot be honoured is signalled as an
   * exception.
   *
   * @param argv the TestNG command line parameters
   * @param listener an optional listener to register before the run, may be {@code null}
   * @return the {@link TestNG} instance that was run
   * @throws TestNGException when {@code argv} cannot be parsed or does not select anything to run.
   *     The message is meant to be shown to the user as is.
   */
  TestNG run(String[] argv, ITestListener listener);

  /**
   * Prints the command line usage banner. Callers treat a throw as "no banner available" and fall
   * back to a terse built-in one.
   */
  void usage();
}
