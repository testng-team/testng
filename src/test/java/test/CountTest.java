package test;

import org.testng.Assert;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;

import java.util.List;

public class CountTest extends SimpleBaseTest {

  @Test(description = "Make sure that skipped methods are accurately counted")
  public void skippedMethodsShouldBeCounted() {
    TestNG tng = create(CountSampleTest.class);

    IReporter r = new IReporter() {
      @Override
      public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
          String outputDirectory) {
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
