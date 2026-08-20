package org.testng.reporters;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.testng.ITestNGListener;
import org.testng.TestNG;

/**
 * What a reporter printed. The built-in reporters write to {@link System#out}, so reading one back
 * means standing in for the stream while a run happens -- the one part of these tests that leaks
 * into the whole JVM if the restore is missed, which is why there is one copy of it.
 */
final class ReportedOutput {

  private static final Charset CHARSET = StandardCharsets.UTF_8;

  private ReportedOutput() {
    // Utility class. Defeat instantiation.
  }

  /**
   * @param testng - The run to report on, ready but not started.
   * @param reporter - The reporter to run it under.
   * @return - Everything written to standard output while it ran.
   */
  static String of(TestNG testng, ITestNGListener reporter) {
    PrintStream currentStream = System.out;
    try {
      ByteArrayOutputStream captured = new ByteArrayOutputStream();
      System.setOut(new PrintStream(captured, true, CHARSET));
      testng.addListener(reporter);
      testng.run();
      return captured.toString(CHARSET);
    } finally {
      System.setOut(currentStream);
    }
  }
}
