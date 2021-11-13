package test.reports;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.testng.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.reporters.FailedReporter;
import org.testng.reporters.TestHTMLReporter;
import org.testng.reporters.TextReporter;
import org.testng.xml.XmlSuite;
import org.testng.xml.internal.Parser;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.TestHelper;
import test.reports.issue1756.CustomTestNGReporter;
import test.reports.issue1756.SampleTestClass;
import test.simple.SimpleSample;

public class ReportTest extends SimpleBaseTest {

  @Test
  public void verifyIndex() throws IOException {
    Path outputDir = TestHelper.createRandomDirectory();

    String suiteName = "VerifyIndexSuite";
    String testName = "TmpTest";
    XmlSuite suite = createXmlSuite(suiteName, testName, SimpleSample.class);

    TestNG tng = create(outputDir, suite);
    tng.addListener(new TestHTMLReporter());

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
    tng.addListener(new TestHTMLReporter());

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
    tng.addListener(new TestHTMLReporter());

    Path f1 = getHtmlReportFile(outputDirectory, suiteNameA, testName);
    Path f2 = getHtmlReportFile(outputDirectory, suiteNameB, testName);

    tng.run();

    Assert.assertTrue(Files.exists(f1));
    Assert.assertTrue(Files.exists(f2));
  }

  private static Path getHtmlReportFile(Path outputDir, String suiteName, String testName)
      throws IOException {
    Path f = outputDir.resolve(Paths.get(suiteName, testName + ".html"));
    Files.deleteIfExists(f);
    return f;
  }

  @Test
  public void shouldHonorSuiteName() throws IOException {
    Path outputDirectory = TestHelper.createRandomDirectory();

    TestNG tng = create(outputDirectory, SampleA.class, SampleB.class);
    tng.addListener(new TestHTMLReporter());

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

    ITestListener listener =
        new TestListenerAdapter() {
          @Override
          public void onTestSuccess(ITestResult tr) {
            super.onTestSuccess(tr);
            List<String> output = Reporter.getOutput(tr);
            ReportTest.m_success = (output != null && output.size() > 0);
          }
        };
    tng.addListener(listener);
    tng.run();

    Assert.assertTrue(m_success);
  }

  @Test
  public void reportLogShouldBeAvailableWithListener() {
    TestNG tng = create(ListenerReporterSample.class);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

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
    tng.addListener(reporter);
    tng.run();

    List<Object[]> parameters = reporter.getParameters();
    Assert.assertEquals(parameters.size(), 5);
    Assert.assertEquals(parameters.get(0)[0].toString(), "[]");
    Assert.assertNull(parameters.get(0)[1]);
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
    Assert.assertNull(parameters.get(4)[1]);
    Assert.assertEquals(parameters.get(4)[2].toString(), "[null, dup, dup, str, null]");
  }

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {
      {
        GitHub1148Sample.class,
        new String[] {"verifyData(Cedric)"},
        new String[] {"verifyData(Anne)"}
      },
      {
        GitHub148Sample.class,
        new String[] {"testMethod(1)", "testMethod(2)"},
        new String[] {"testMethod(3)"}
      }
    };
  }

  @Test(dataProvider = "dp")
  public void runFailedTestTwiceShouldBeConsistent(
      Class<?> testClass, String[] succeedMethods, String[] failedMethods) throws IOException {
    Path outputDirectory = TestHelper.createRandomDirectory();

    TestNG tng = create(outputDirectory, testClass);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.addListener(new FailedReporter());

    tng.run();

    assertThat(listener.getFailedMethodNames()).containsExactly(failedMethods);
    assertThat(listener.getSucceedMethodNames()).containsExactly(succeedMethods);
    assertThat(listener.getSkippedMethodNames()).isEmpty();

    Path testngFailedXml = outputDirectory.resolve(FailedReporter.TESTNG_FAILED_XML);
    assertThat(testngFailedXml).exists();

    for (int i = 0; i < 5; i++) {
      testngFailedXml = checkFailed(testngFailedXml, failedMethods);
    }
  }

  @Test(description = "GITHUB-1756")
  public void testToEnsureSkippedTestsHaveProviderITestNameRetrieved() {
    TestNG testng = create(SampleTestClass.class);
    CustomTestNGReporter reporter = new CustomTestNGReporter();
    testng.addListener(reporter);
    testng.run();
    assertThat(reporter.getLogs())
        .containsExactly(SampleTestClass.getUuid(), SampleTestClass.getUuid());
  }

  private static Path checkFailed(Path testngFailedXml, String... failedMethods)
      throws IOException {
    Path outputDirectory = TestHelper.createRandomDirectory();

    List<XmlSuite> suites = new Parser(Files.newInputStream(testngFailedXml)).parseToList();
    TestNG tng = create(outputDirectory, suites);
    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);
    tng.addListener(new FailedReporter());

    tng.run();

    assertThat(listener.getSucceedMethodNames()).isEmpty();
    assertThat(listener.getSkippedMethodNames()).isEmpty();
    assertThat(listener.getFailedMethodNames()).containsExactly(failedMethods);

    Path testngFailedXml2 = outputDirectory.resolve(FailedReporter.TESTNG_FAILED_XML);
    assertThat(testngFailedXml2).exists();

    return testngFailedXml2;
  }

  public static class DpArrays {
    public enum Item {
      ITEM1,
      ITEM2
    }

    @DataProvider
    public static Object[][] dpArrays() {
      return new Object[][] {{new Item[] {Item.ITEM1}}, {new Item[] {Item.ITEM1, Item.ITEM2}}};
    }

    @Test(dataProvider = "dpArrays")
    public void testMethod(Item[] strings) {}
  }

  public static class NullParameter {
    @DataProvider
    public static Object[][] nullProvider() {
      return new Object[][] {{null, "Bazinga!"}};
    }

    @Test(dataProvider = "nullProvider")
    public void testMethod(Object nullReference, String bazinga) {}
  }

  @Test
  public void reportArraysToString() {
    TestNG tng = create(DpArrays.class);
    tng.addListener(new TextReporter("name", 2));

    PrintStream previousOut = System.out;
    ByteArrayOutputStream systemOutCapture = new ByteArrayOutputStream();
    System.setOut(new PrintStream(systemOutCapture));
    tng.run();
    System.setOut(previousOut);

    Assert.assertTrue(systemOutCapture.toString().contains("testMethod([ITEM1])"));
    Assert.assertTrue(systemOutCapture.toString().contains("testMethod([ITEM1, ITEM2])"));
  }

  @Test
  public void reportCreatedWithNullParameter() {
    TestNG tng = create(NullParameter.class);
    tng.addListener(new TextReporter("name", 2));

    PrintStream previousOut = System.out;
    ByteArrayOutputStream systemOutCapture = new ByteArrayOutputStream();
    System.setOut(new PrintStream(systemOutCapture));
    tng.run();
    System.setOut(previousOut);

    Assert.assertTrue(
        systemOutCapture.toString().contains("PASSED: testMethod(null, \"Bazinga!\")"));
  }
}
