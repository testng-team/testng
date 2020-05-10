package org.testng;

import org.testng.reporters.IReporterConfig;
import org.testng.reporters.PojoReporterConfig;
import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * This interface can be implemented by clients to generate a report. Its method generateReport()
 * will be invoked after all the suite have run and the parameters give all the test results that
 * happened during that run.
 * <p>
 * <b>NOTE</b>: Reporters that include configurable properties must adhere to JavaBean object
 * conventions, providing getter and setter methods that conform to standard naming rules.
 * This enables {@link ReporterConfig} to serialize, deserialize, and instantiate the reporter.
 *
 * @author cbeust Feb 17, 2006
 */
public interface IReporter extends ITestNGListener {
  /** Generate a report for the given suites into the specified output directory. */
  default void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    // not implemented
  }

  default IReporterConfig getConfig() {
    return new PojoReporterConfig(this);
  }
}
