package test.factory.issue1041;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.collections.Maps;
import org.testng.xml.XmlSuite;

public class ResultExtractor implements IReporter {

  private Map<Object, Object[]> data = Maps.newHashMap();

  @Override
  public void generateReport(
      List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
    suites.forEach(this::process);
  }

  private void process(ISuite suite) {
    suite.getResults().values().forEach(this::process);
  }

  private void process(ISuiteResult suiteResult) {
    ITestContext ctx = suiteResult.getTestContext();
    List<IResultMap> resultmaps =
        Arrays.asList(ctx.getFailedTests(), ctx.getPassedTests(), ctx.getSkippedTests());
    resultmaps.forEach(this::process);
  }

  private void process(IResultMap resultMap) {
    resultMap.getAllResults().forEach(this::process);
  }

  private void process(ITestResult result) {
    data.put(result.getInstance(), result.getFactoryParameters());
  }

  public Object[] getData(Object key) {
    return data.get(key);
  }
}
