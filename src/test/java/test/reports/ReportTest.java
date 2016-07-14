package test.reports;

import org.testng.*;
import org.testng.annotations.Test;
import org.testng.reporters.TextReporter;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.TestHelper;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ReportTest {

  @Test
  public void verifyIndex() {
    File outputDir = TestHelper.createRandomDirectory();

    String suiteName = "VerifyIndexSuite";
    String testName = "TmpTest";
    XmlSuite suite = TestHelper.createSuite(test.simple.SimpleTest.class, suiteName, testName);

    TestNG tng = TestHelper.createTestNG(suite, outputDir.getAbsolutePath());

    File f = getHtmlReportFile(outputDir, suiteName, testName);

    tng.run();

    Assert.assertTrue(f.exists());
  }

  @Test
  public void directoryShouldBeSuiteName() {
    File outputDirectory = TestHelper.createRandomDirectory();

    XmlSuite xmlSuite = new XmlSuite();
    String suiteName = "ReportTestSuite1";
    xmlSuite.setName(suiteName);

    XmlTest xmlTest = new XmlTest(xmlSuite);
    String testName = "Test1";
    xmlTest.setName(testName);

    XmlTest xmlTest2 = new XmlTest(xmlSuite);
    String testName2 = "Test2";
    xmlTest2.setName(testName2);

    TestNG testng = new TestNG();
    testng.setVerbose(0);
    testng.setOutputDirectory(outputDirectory.getAbsolutePath());
    testng.setXmlSuites(Arrays.asList(xmlSuite));

    File f = getHtmlReportFile(outputDirectory, suiteName, testName);
    File f2 = getHtmlReportFile(outputDirectory, suiteName, testName2);

    testng.run();

    Assert.assertTrue(f.exists(), "Expected to find file:" + f);
    Assert.assertTrue(f2.exists(), "Expected to find file:" + f2);
  }

  @Test
  public void oneDirectoryPerSuite() {
    File outputDirectory = TestHelper.createRandomDirectory();

    String testName = "TmpTest";
    String suiteNameA = "ReportSuiteA";
    XmlSuite xmlSuiteA = TestHelper.createSuite(test.reports.A.class, suiteNameA, testName);

    String suiteNameB = "ReportSuiteB";
    XmlSuite xmlSuiteB = TestHelper.createSuite(test.reports.B.class, suiteNameB, testName);

    TestNG testng = TestHelper.createTestNG();
    testng.setOutputDirectory(outputDirectory.getAbsolutePath());
    testng.setXmlSuites(Arrays.asList(xmlSuiteA, xmlSuiteB));

    File f1 = getHtmlReportFile(outputDirectory, suiteNameA, testName);
    File f2 = getHtmlReportFile(outputDirectory, suiteNameB, testName);

    testng.run();

    Assert.assertTrue(f1.exists());
    Assert.assertTrue(f2.exists());
  }

  private static File getHtmlReportFile(File outputDir, String suiteName, String testName) {
    File f = new File(outputDir.getAbsolutePath() + File.separatorChar + suiteName
                    + File.separatorChar + testName + ".html");
    if (f.exists()) {
      f.delete();
    }
    return f;
  }

  @Test
  public void shouldHonorSuiteName() {
    TestNG testng = TestHelper.createTestNG();
    testng.setTestClasses(new Class[] { A.class, B.class });
    String outputDir = testng.getOutputDirectory();

    String dirA = outputDir + File.separatorChar + "SuiteA-JDK5";
    File fileA = new File(dirA);
    String dirB = outputDir + File.separatorChar + "SuiteB-JDK5";
    File fileB = new File(dirB);
    Assert.assertFalse(fileA.exists());
    Assert.assertFalse(fileB.exists());

    testng.run();

    Assert.assertTrue(fileA.exists(), fileA + " wasn't created");
    Assert.assertTrue(fileB.exists(), fileB + " wasn't created");
  }

  static boolean m_success = false;

  @Test
  public void reportLogShouldBeAvailableEvenWithTimeOut() {
    m_success = false;
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setTestClasses(new Class[] { ReporterSampleTest.class });

    ITestListener listener = new TestListenerAdapter() {
      @Override
      public void onTestSuccess(ITestResult tr) {
        super.onTestSuccess(tr);
        List<String> output = Reporter.getOutput(tr);
        m_success = output != null && output.size() > 0;
      }
    };
    tng.addListener(listener);
    tng.run();

    Assert.assertTrue(m_success);
  }

  @Test(description = "GITHUB-1090")
  public void github1090() {
    TestNG tng = new TestNG();
    tng.setVerbose(0);
    tng.setTestClasses(new Class[]{GitHub447Sample.class});
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
