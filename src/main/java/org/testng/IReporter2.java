package org.testng;

import org.testng.reporters.XMLReporterConfig;
import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * This interface can be implemented by clients to generate a report.  Its method
 * generateReport() will be invoked after all the suite have run and the parameters
 * give all the test results that happened during that run.
 */
public interface IReporter2 extends ITestNGListener {
  /**
   * Generate a report for the given suites with specified attributes
   */
  void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, XMLReporterConfig config);
}
