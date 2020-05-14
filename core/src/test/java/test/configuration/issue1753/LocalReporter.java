package test.configuration.issue1753;

import org.testng.*;
import org.testng.collections.Maps;
import org.testng.xml.XmlSuite;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class LocalReporter implements IReporter {

  private Map<String, String> attributes = Maps.newHashMap();

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    suites.forEach(
        suite ->
            suite
                .getResults()
                .values()
                .stream()
                .flatMap(iSuiteResult -> extractSkippedResults(iSuiteResult).stream())
                .forEach(this::extractAttributes));
  }

  private void extractAttributes(ITestResult result) {
    Consumer<String> attribute = each -> attributes.put(each, result.getAttribute(each).toString());
    result.getAttributeNames().forEach(attribute);
  }

  private static Set<ITestResult> extractSkippedResults(ISuiteResult suiteResult) {
    return suiteResult.getTestContext().getSkippedTests().getAllResults();
  }

  Map<String, String> getAttributes() {
    return attributes;
  }
}
