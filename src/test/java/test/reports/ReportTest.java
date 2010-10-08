package test.reports;

import org.testng.Assert;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.TestHelper;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ReportTest {

  @Test
  public void verifyIndex() {
    String suiteName = "VerifyIndexSuite";
    File outputDir = TestHelper.createRandomDirectory();
    XmlSuite suite = TestHelper.createSuite("test.simple.SimpleTest", suiteName);

    File f = new File(outputDir.getAbsolutePath() + File.separatorChar + suiteName
        + File.separatorChar + "index.html");
    f.delete();
    Assert.assertFalse(f.exists());

    TestNG tng = TestHelper.createTestNG(suite, outputDir.getAbsolutePath());
    tng.run();
    Assert.assertTrue(f.exists());

    f.deleteOnExit();
  }

  @Test
  public void directoryShouldBeSuiteName() {
    String outputDirectory = TestHelper.createRandomDirectory().getAbsolutePath();

    TestNG testng = new TestNG();
    testng.setVerbose(0);
    testng.setOutputDirectory(outputDirectory);

    XmlSuite xmlSuite = new XmlSuite();
    String suiteName = "ReportTestSuite1";
    xmlSuite.setName(suiteName);

    XmlTest xmlTest = new XmlTest(xmlSuite);
    xmlTest.setName("Test1");

    testng.setXmlSuites(Arrays.asList(new XmlSuite[] { xmlSuite }));

    File indexFile =
      new File(outputDirectory + File.separatorChar + suiteName + File.separatorChar + "index.html");
    indexFile.delete();
    Assert.assertFalse(indexFile.exists());

    testng.run();

    Assert.assertTrue(indexFile.exists(), "Expected to find file:" + indexFile);
  }

  @Test
  public void oneDirectoryPerSuite() {
    XmlSuite xmlSuiteA = TestHelper.createSuite("test.reports.A", "ReportSuiteA");
    XmlSuite xmlSuiteB = TestHelper.createSuite("test.reports.B", "ReportSuiteB");
    TestNG testng = TestHelper.createTestNG();
    testng.setXmlSuites(Arrays.asList(new XmlSuite[] { xmlSuiteA, xmlSuiteB }));


    String outputDir = testng.getOutputDirectory();
    File f1 = new File(outputDir + File.separatorChar + xmlSuiteA.getName()
        + File.separatorChar + "index.html");

    File f2 = new File(outputDir + File.separatorChar + xmlSuiteB.getName()
        + File.separatorChar + "index.html");

    Assert.assertFalse(f1.exists());
    Assert.assertFalse(f2.exists());

    testng.run();

    Assert.assertTrue(f1.exists());
    Assert.assertTrue(f2.exists());
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
//        ppp("ON SUCCESS, OUTPUT:" + output + " SUCCESS:" + m_success);
      }
    };
    tng.addListener(listener);
    tng.run();

    Assert.assertTrue(m_success);
  }

  private static void ppp(String s) {
    System.out.println("[ReporterTest] " + s);
  }
}
