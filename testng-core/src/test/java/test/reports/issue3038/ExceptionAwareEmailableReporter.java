package test.reports.issue3038;

import java.util.List;
import org.testng.ISuite;
import org.testng.reporters.EmailableReporter2;
import org.testng.xml.XmlSuite;

public class ExceptionAwareEmailableReporter extends EmailableReporter2 {

  public boolean hasError = false;

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    try {
      super.generateReport(xmlSuites, suites, outputDirectory);
    } catch (IllegalStateException e) {
      hasError = true;
      throw e;
    }
  }
}
