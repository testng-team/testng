package test.retryAnalyzer.issue1697;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

public class LocalReporter implements IReporter {
  private Set<ITestResult> skipped = Collections.newSetFromMap(new ConcurrentHashMap<>());
  private Set<ITestResult> retried = Collections.newSetFromMap(new ConcurrentHashMap<>());

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    suites.stream()
        .map(
            suite -> {
              Set<ITestResult> results = new HashSet<>();
              Collection<ISuiteResult> values = suite.getResults().values();
              for (ISuiteResult value : values) {
                results.addAll(value.getTestContext().getSkippedTests().getAllResults());
              }
              return results;
            })
        .collect(Collectors.toSet())
        .forEach(
            iTestResults -> {
              for (ITestResult iTestResult : iTestResults) {
                if (iTestResult.wasRetried()) {
                  retried.add(iTestResult);
                } else {
                  skipped.add(iTestResult);
                }
              }
            });
  }

  public Set<ITestResult> getRetried() {
    return retried;
  }

  public Set<ITestResult> getSkipped() {
    return skipped;
  }
}
