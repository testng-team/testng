package test.reports.issue1756;

import java.util.LinkedList;
import java.util.List;
import org.testng.*;
import org.testng.xml.XmlSuite;

public class CustomTestNGReporter implements IReporter {
  private final List<String> logs = new LinkedList<>();

  public List<String> getLogs() {
    return logs;
  }

  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    getTestMehodSummary(suites);
  }

  private void getTestMehodSummary(List<ISuite> suites) {
    suites.stream()
        .flatMap(it -> it.getResults().values().stream())
        .map(ISuiteResult::getTestContext)
        .forEach(
            testObj -> {
              getTestMethodReport(testObj.getFailedTests());
              getTestMethodReport(testObj.getSkippedTests());
              getTestMethodReport(testObj.getPassedTests());
            });
  }

  private void getTestMethodReport(IResultMap testResultMap) {
    testResultMap.getAllResults().forEach(iTestResult -> logs.add(iTestResult.getName()));
  }
}
