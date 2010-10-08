package org.testng;

import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * This interface can be implemented by clients to generate a report.  Its method
 * generateReport() will be invoked after all the suite have run and the parameters
 * give all the test results that happened during that run.
 *
 * @author cbeust
 * Feb 17, 2006
 */
public interface IReporter extends ITestNGListener {
  /**
   * Generate a report for the given suites into the specified output directory.
   */
  void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory);
}
