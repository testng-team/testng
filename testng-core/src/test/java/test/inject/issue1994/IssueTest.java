package test.inject.issue1994;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.testng.IConfigurationListener;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(description = "GITHUB-1994")
  public void injectedXmlTestIsNotClonedIntoTheRunningSuite() {
    XmlSuite suite = createXmlSuite("issue1994-suite");
    XmlTest xmlTest = createXmlTest(suite, "issue1994-test", XmlTestInjectionSample.class);

    Collector collector = new Collector();
    TestNG tng = create(suite);
    tng.addListener(collector);
    tng.run();

    assertThat(suite.getTests()).hasSize(1);
    assertThat(suite.getTests().get(0)).isSameAs(xmlTest);

    // isSameAs, not containsExactly: XmlTest overrides equals, so a clone compares equal to its
    // original and equality-based assertions would not discriminate.
    assertThat(collector.reported)
        .hasSize(3)
        .allSatisfy(reported -> assertThat(reported).isSameAs(xmlTest));
  }

  /** Collects the {@link XmlTest} a result reports, which is the view a reporter would get. */
  private static class Collector implements ITestListener, IConfigurationListener {

    private final List<XmlTest> reported = new ArrayList<>();

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
  }
}
