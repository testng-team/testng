package test.testng674;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.testng.*;
import org.testng.xml.XmlSuite;

public class ReportingListenerFor674 implements IReporter {
  private List<Throwable> errors = new ArrayList<>();

  public void generateReport(List<XmlSuite> list, List<ISuite> suites, String s) {
    for (ISuite suite : suites) {
      for (ISuiteResult suiteResult : suite.getResults().values()) {
        ITestContext ctx = suiteResult.getTestContext();
        Set<ITestResult> results = ctx.getSkippedTests().getAllResults();
        for (ITestResult result : results) {
          Throwable throwable = result.getThrowable();
          errors.add(throwable);
        }
      }
    }
  }

  public List<Throwable> getErrors() {
    return errors;
  }
}
