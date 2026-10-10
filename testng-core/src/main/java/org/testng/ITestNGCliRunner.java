package org.testng;

import org.jspecify.annotations.Nullable;

/**
 * Runs TestNG from the command line for {@link TestNG#main(String[])}.
 *
 * <p>TestNG finds the implementation with {@link java.util.ServiceLoader}. This keeps the command
 * line parsing library out of {@code testng-core}. The {@code testng-jcommander} module has the
 * default implementation, and the {@code org.testng:testng} jar includes it.
 *
 * <p>Other programs can call {@link #run(String[], ITestListener)} directly, not only through
 * {@link TestNG#main(String[])}. So an implementation:
 *
 * <ul>
 *   <li>Never stops the JVM.
 *   <li>Prints the usage text only when {@link #usage()} asks for it.
 * </ul>
 *
 * <p>{@link TestNG#main(String[])} decides what happens to the process after a bad command line.
 *
 * @since 7.13
 */
public interface ITestNGCliRunner {

  /**
   * Parses the command line, sets up a new {@link TestNG} object, and runs it.
   *
   * <p>A failed test does <em>not</em> throw. The returned object holds the result in {@link
   * TestNG#getStatus()}. When the run itself throws a {@link TestNGException}, this method reports
   * it through {@link TestNG#reportRunFailure(TestNGException)}, and does not throw it.
   *
   * <p>Any other exception reaches the caller. For example, a {@link RuntimeException} from a
   * listener passes through this method.
   *
   * @param argv the TestNG command line arguments.
   * @param listener a listener that TestNG adds before the run, or {@code null}.
   * @return the {@link TestNG} object that ran.
   * @throws TestNGException when this method cannot parse {@code argv}, or {@code argv} selects
   *     nothing to run. The message is ready to show to the user as it is.
   */
  TestNG run(String[] argv, @Nullable ITestListener listener);

  /**
   * Prints the command line usage text.
   *
   * <p>When this method throws a {@link RuntimeException}, TestNG prints a short built-in usage
   * text instead.
   */
  void usage();
}
