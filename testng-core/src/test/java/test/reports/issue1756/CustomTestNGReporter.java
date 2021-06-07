package test.reports.issue1756;

import java.util.LinkedList;
import java.util.List;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.xml.XmlSuite;

public class CustomTestNGReporter implements IReporter {
  private List<String> logs = new LinkedList<>();

  public List<String> getLogs() {
    return logs;
  }

  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    getTestMehodSummary(suites);
  }

  private void getTestMehodSummary(List<ISuite> suites) {
    suites.forEach(
        iSuite ->
            iSuite
                .getResults()
                .values()
                .forEach(
                    each -> {
                      ITestContext testObj = each.getTestContext();
                      getTestMethodReport(testObj.getFailedTests());
                      getTestMethodReport(testObj.getSkippedTests());
                      getTestMethodReport(testObj.getPassedTests());
                    }));
  }

  private void getTestMethodReport(IResultMap testResultMap) {
    testResultMap.getAllResults().forEach(iTestResult -> logs.add(iTestResult.getName()));
  }
}
