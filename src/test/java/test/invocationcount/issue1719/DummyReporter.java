package test.invocationcount.issue1719;

import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestResult;
import org.testng.collections.Sets;
import org.testng.xml.XmlSuite;

import java.util.List;
import java.util.Set;

public class DummyReporter implements IReporter {
  private Set<ITestResult> failures = Sets.newHashSet();
  private Set<ITestResult> skip = Sets.newHashSet();
  private Set<ITestResult> success = Sets.newHashSet();
  private Set<ITestResult> failedWithinSuccessPercentage = Sets.newHashSet();

  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    suites.forEach(
        iSuite ->
            iSuite
                .getResults()
                .values()
                .forEach(
                    suiteResult -> {
                      failures.addAll(
                          suiteResult.getTestContext().getFailedTests().getAllResults());
                      skip.addAll(suiteResult.getTestContext().getSkippedTests().getAllResults());
                      success.addAll(suiteResult.getTestContext().getPassedTests().getAllResults());
                      failedWithinSuccessPercentage.addAll(
                          suiteResult
                              .getTestContext()
                              .getFailedButWithinSuccessPercentageTests()
                              .getAllResults());
                    }));
  }

  public Set<ITestResult> getFailures() {
    return failures;
  }

  public Set<ITestResult> getSkip() {
    return skip;
  }

  public Set<ITestResult> getSuccess() {
    return success;
  }

  public Set<ITestResult> getFailedWithinSuccessPercentage() {
    return failedWithinSuccessPercentage;
  }
}
