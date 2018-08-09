package test.skip;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.collections.Maps;
import org.testng.xml.XmlSuite;

public class ReasonReporter implements IReporter {

  private Map<String, String> skippedInfo = Maps.newHashMap();

  public Map<String, String> getSkippedInfo() {
    return skippedInfo;
  }

  @Override
  public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outDir) {
    suites.stream()
        .flatMap(suite -> suite.getResults().values().stream())
        .flatMap(suiteResult -> suiteResult.getTestContext().getSkippedTests().getAllResults().stream())
        .forEach(this::generateReport);
  }

  public void generateReport(ITestResult result) {
    String text = result.getSkipCausedBy().stream()
        .map(ITestNGMethod::getMethodName)
        .collect(Collectors.joining(","));
    skippedInfo.put(result.getMethod().getMethodName(), text);
  }
}
