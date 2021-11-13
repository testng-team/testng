package test.name;

import java.util.ArrayList;
import java.util.List;
import org.testng.IInvokedMethod;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

public class TestOnClassListener implements IReporter {

  private final List<String> names = new ArrayList<>();
  private final List<String> testNames = new ArrayList<>();

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    for (ISuite suite : suites) {
      for (IInvokedMethod method : suite.getAllInvokedMethods()) {
        names.add(method.getTestResult().getName());
        testNames.add(method.getTestResult().getTestName());
      }
    }
  }

  public List<String> getNames() {
    return names;
  }

  public List<String> getTestNames() {
    return testNames;
  }
}
