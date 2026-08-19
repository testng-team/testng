package test.inject.issue1994;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.IConfigurationListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.xml.XmlTest;

/**
 * Collects every {@link XmlTest} a result reports through {@code ITestResult.getParameters()},
 * which is what a reporter would see.
 */
public class InjectedXmlTestCollector implements ITestListener, IConfigurationListener {

  private final List<XmlTest> reported = Collections.synchronizedList(new ArrayList<>());

  @Override
  public void onTestSuccess(ITestResult result) {
    record(result);
  }

  @Override
  public void onConfigurationSuccess(ITestResult tr) {
    record(tr);
  }

  private void record(ITestResult result) {
    for (Object parameter : result.getParameters()) {
      if (parameter instanceof XmlTest) {
        reported.add((XmlTest) parameter);
      }
    }
  }

  public List<XmlTest> getReported() {
    return reported;
  }
}
