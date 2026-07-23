package test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

public class CountTest extends SimpleBaseTest {

  @Test(description = "Make sure that skipped methods are accurately counted")
  public void skippedMethodsShouldBeCounted() {
    TestNG tng = create(CountSampleTest.class);

    IReporter r =
        new IReporter() {
          @Override
          public void generateReport(
              List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
            for (ISuite s : suites) {
              for (ISuiteResult sr : s.getResults().values()) {
                ITestContext ctx = sr.getTestContext();
                assertThat(2).isEqualTo(ctx.getSkippedTests().size());
              }
            }
          }
        };

    tng.addListener(r);
    tng.run();
  }
}
