package test.reports;

import org.testng.*;
import org.testng.annotations.Test;
import org.testng.reporters.TestHTMLReporter;
import org.testng.xml.XmlSuite;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.TestHelper;
import test.simple.SimpleSample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportTest extends SimpleBaseTest {

  @Test
  public void verifyIndex() throws IOException {
    Path outputDir = TestHelper.createRandomDirectory();

    String suiteName = "VerifyIndexSuite";
    String testName = "TmpTest";
    XmlSuite suite = createXmlSuite(suiteName, testName, SimpleSample.class);

    TestNG tng = create(outputDir, suite);
    tng.addListener((ITestNGListener) new TestHTMLReporter());

    Path f = getHtmlReportFile(outputDir, suiteName, testName);

    tng.run();

    Assert.assertTrue(Files.exists(f), f.toString());
  }

  @Test
  public void directoryShouldBeSuiteName() throws IOException {
    Path outputDirectory = TestHelper.createRandomDirectory();

    String suiteName = "ReportTestSuite1";
    String testName = "Test1";
    String testName2 = "Test2";
    XmlSuite xmlSuite = createXmlSuite(suiteName);
    createXmlTest(xmlSuite, testName);
    createXmlTest(xmlSuite, testName2);

    TestNG tng = create(outputDirectory, xmlSuite);
    tng.addListener((ITestNGListener) new TestHTMLReporter());

    Path f = getHtmlReportFile(outputDirectory, suiteName, testName);
    Path f2 = getHtmlReportFile(outputDirectory, suiteName, testName2);

    tng.run();

    Assert.assertTrue(Files.exists(f));
    Assert.assertTrue(Files.exists(f2));
  }

  @Test
  public void oneDirectoryPerSuite() throws IOException {
    Path outputDirectory = TestHelper.createRandomDirectory();

    String suiteNameA = "ReportSuiteA";
    String suiteNameB = "ReportSuiteB";
    String testName = "TmpTest";
    XmlSuite xmlSuiteA = createXmlSuite(suiteNameA, testName, SampleA.class);
    XmlSuite xmlSuiteB = createXmlSuite(suiteNameB, testName, SampleB.class);

    TestNG tng = create(outputDirectory, xmlSuiteA, xmlSuiteB);
    tng.addListener((ITestNGListener) new TestHTMLReporter());

    Path f1 = getHtmlReportFile(outputDirectory, suiteNameA, testName);
    Path f2 = getHtmlReportFile(outputDirectory, suiteNameB, testName);

    tng.run();

    Assert.assertTrue(Files.exists(f1));
    Assert.assertTrue(Files.exists(f2));
  }

  private static Path getHtmlReportFile(Path outputDir, String suiteName, String testName) throws IOException {
    Path f = outputDir.resolve(Paths.get(suiteName, testName + ".html"));
    Files.deleteIfExists(f);
    return f;
  }

  @Test
  public void shouldHonorSuiteName() throws IOException {
    Path outputDirectory = TestHelper.createRandomDirectory();

    TestNG tng = create(outputDirectory, SampleA.class, SampleB.class);
    tng.addListener((ITestNGListener) new TestHTMLReporter());

    Path fileA = outputDirectory.resolve("SuiteA-JDK5");
    Path fileB = outputDirectory.resolve("SuiteB-JDK5");
    Assert.assertTrue(Files.notExists(fileA));
    Assert.assertTrue(Files.notExists(fileB));

    tng.run();

    Assert.assertTrue(Files.exists(fileA));
    Assert.assertTrue(Files.exists(fileB));
  }

  private static boolean m_success;

  @Test
  public void reportLogShouldBeAvailableEvenWithTimeOut() {
    m_success = false;
    TestNG tng = create(ReporterSample.class);

    ITestListener listener = new TestListenerAdapter() {
      @Override
      public void onTestSuccess(ITestResult tr) {
        super.onTestSuccess(tr);
        List<String> output = Reporter.getOutput(tr);
        ReportTest.m_success = (output != null && output.size() > 0);
      }
    };
    tng.addListener((ITestNGListener) listener);
    tng.run();

    Assert.assertTrue(m_success);
  }

  @Test
  public void reportLogShouldBeAvailableWithListener() {
    TestNG tng = create(ListenerReporterSample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener((ITestNGListener) listener);

    Reporter.clear();

    tng.run();

    assertThat(listener.getFailedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getSucceedMethodNames()).containsExactly("testMethod");
    assertThat(Reporter.getOutput()).hasSize(2);
  }

  @Test(description = "GITHUB-1090")
  public void github1090() {
    TestNG tng = create(GitHub447Sample.class);
    GitHub447Listener reporter = new GitHub447Listener();
    tng.addListener((ITestNGListener) reporter);
    tng.run();

    List<Object[]> parameters = reporter.getParameters();
    Assert.assertEquals(parameters.size(), 5);
    Assert.assertEquals(parameters.get(0)[0].toString(), "[]");
    Assert.assertEquals(parameters.get(0)[1], null);
    Assert.assertEquals(parameters.get(0)[2].toString(), "[null]");
    Assert.assertEquals(parameters.get(1)[0].toString(), "[null]");
    Assert.assertEquals(parameters.get(1)[1], "dup");
    Assert.assertEquals(parameters.get(1)[2].toString(), "[null, dup]");
    Assert.assertEquals(parameters.get(2)[0].toString(), "[null, dup]");
    Assert.assertEquals(parameters.get(2)[1], "dup");
    Assert.assertEquals(parameters.get(2)[2].toString(), "[null, dup, dup]");
    Assert.assertEquals(parameters.get(3)[0].toString(), "[null, dup, dup]");
    Assert.assertEquals(parameters.get(3)[1], "str");
    Assert.assertEquals(parameters.get(3)[2].toString(), "[null, dup, dup, str]");
    Assert.assertEquals(parameters.get(4)[0].toString(), "[null, dup, dup, str]");
    Assert.assertEquals(parameters.get(4)[1], null);
    Assert.assertEquals(parameters.get(4)[2].toString(), "[null, dup, dup, str, null]");
  }
}
