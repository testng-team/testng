package org.testng;

import java.util.List;
import org.testng.reporters.IReporterConfig;
import org.testng.reporters.PojoReporterConfig;
import org.testng.xml.XmlSuite;

/**
 * This interface can be implemented by clients to generate a report. Its method generateReport()
 * will be invoked after all the suite have run and the parameters give all the test results that
 * happened during that run.
 */
public interface IReporter extends ITestNGListener {
  /**
   * Generate a report for the given suites into the specified output directory.
   *
   * @param xmlSuites The list of <code>XmlSuite</code>
   * @param suites The list of <code>ISuite</code>
   * @param outputDirectory The output directory
   */
  default void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    // not implemented
  }

  /**
   * Get the reporter configuration object.
   *
   * <p><b>NOTE</b>: Reporter configuration objects must adhere to the JavaBean object conventions,
   * providing getter and setter methods that conform to standard naming rules. This enables {@link
   * org.testng.internal.ReporterConfig} to serialize, deserialize, and instantiate the reporter.
   *
   * @return reporter configuration object
   */
  default IReporterConfig getConfig() {
    return new PojoReporterConfig(this);
  }
}
