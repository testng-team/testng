package test.dataprovider.issue2980;

import java.util.List;
import java.util.stream.Collectors;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.internal.collections.Pair;
import org.testng.xml.XmlSuite;

public class LoggingListener implements IReporter {

  private List<Pair<String, Integer>> pairs;

  @Override
  @SuppressWarnings("unchecked")
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    pairs =
        suites.stream()
            .flatMap(it -> it.getResults().values().stream())
            .map(ISuiteResult::getTestContext)
            .flatMap(it -> it.getPassedTests().getAllResults().stream())
            .map(it -> it.getAttribute(TestClassSample.THREAD_ID))
            .map(it -> (Pair<String, Integer>) it)
            .collect(Collectors.toList());
  }

  public final List<Integer> getThreadIds() {
    return pairs.stream().map(Pair::second).distinct().collect(Collectors.toList());
  }

  public final List<String> getMethodNames() {
    return pairs.stream().map(Pair::first).distinct().collect(Collectors.toList());
  }
}
