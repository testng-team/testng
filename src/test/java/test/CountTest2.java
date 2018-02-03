package test;

import org.testng.*;
import org.testng.annotations.Test;
import org.testng.reporters.XMLReporterConfig;
import org.testng.xml.XmlSuite;

import java.util.List;

public class CountTest2 extends SimpleBaseTest {

  @Test(description = "Make sure that skipped methods are accurately counted")
  public void skippedMethodsShouldBeCounted() {
    TestNG tng = create(CountSampleTest.class);

    IReporter2 r = new IReporter2() {
      @Override
      public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
          XMLReporterConfig config) {
        for (ISuite s : suites) {
          for (ISuiteResult sr : s.getResults().values()) {
            ITestContext ctx = sr.getTestContext();
            Assert.assertEquals(2, ctx.getSkippedTests().size());
          }
        }
      }
    };

    tng.addListener(r);
    tng.run();
  }
}
