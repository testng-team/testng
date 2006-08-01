package org.testng;

import java.util.List;

import org.testng.xml.XmlSuite;

/**
 * This interface can be implemented by clients to generate a report.  Its method 
 * generateReport() will be invoked after all the suite have run and the parameters
 * give all the test results that happened during that run.
 * 
 * @author cbeust
 * @date Feb 17, 2006
 */
public interface IReporter {
  /**
   * Generate a report for the given suites into the specified output directory.
   */
  void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory);
}
