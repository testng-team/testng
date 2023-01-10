package test.configurationfailurepolicy.issue2862;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

public class ConfigFailurePolicyAwareReporter implements IReporter {

  private Map<Class<?>, List<ITestResult>> grouped;

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    grouped =
        suites.stream()
            .flatMap(each -> each.getResults().values().stream())
            .map(ISuiteResult::getTestContext)
            .flatMap(each -> results(each).stream())
            .collect(Collectors.groupingBy(ConfigFailurePolicyAwareReporter::clazz));
  }

  public Map<Class<?>, Map<Integer, List<ITestResult>>> getGrouped() {
    return grouped.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, v -> byStatus(v.getValue())));
  }

  private static Class<?> clazz(ITestResult itr) {
    return itr.getMethod().getRealClass();
  }

  private static Map<Integer, List<ITestResult>> byStatus(List<ITestResult> testResults) {
    return testResults.stream().collect(Collectors.groupingBy(ITestResult::getStatus));
  }

  private static Set<ITestResult> results(ITestContext itr) {
    Set<ITestResult> results = new HashSet<>(itr.getSkippedTests().getAllResults());
    results.addAll(itr.getPassedTests().getAllResults());
    results.addAll(itr.getFailedTests().getAllResults());
    return results;
  }
}
