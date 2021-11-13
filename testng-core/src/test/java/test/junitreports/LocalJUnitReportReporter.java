package test.junitreports;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.testng.ISuite;
import org.testng.reporters.JUnitReportReporter;
import org.testng.xml.XmlSuite;

public class LocalJUnitReportReporter extends JUnitReportReporter implements TestsuiteRetriever {
  private List<Testsuite> testsuites = new ArrayList<>();

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String defaultOutputDirectory) {
    super.generateReport(xmlSuites, suites, defaultOutputDirectory);
    String dir = defaultOutputDirectory + File.separator + "junitreports";
    File directory = new File(dir);
    File[] files = directory.listFiles((dir1, name) -> name.endsWith(".xml"));
    testsuites.addAll(LocalJUnitXMLReporter.getSuites(files));
  }

  public Testsuite getTestsuite(String name) {
    for (Testsuite suite : testsuites) {
      if (suite.getName().equals(name)) {
        return suite;
      }
    }
    return null;
  }
}
