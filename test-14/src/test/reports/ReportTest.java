package test.reports;

import java.io.File;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.TestHelper;

public class ReportTest {

  /**
   * @testng.test
   */
  public void verifyIndex() {
    String suiteName = "VerifyIndexSuite";
    File outputDir = TestHelper.createRandomDirectory();
    XmlSuite suite = TestHelper.createSuite("test.reports.A", suiteName);

    File f = new File(outputDir.getAbsolutePath() + File.separatorChar + suiteName
        + File.separatorChar + "index.html");
    f.delete();
    Assert.assertFalse(f.exists());
    
    TestNG tng = TestHelper.createTestNG(suite, outputDir.getAbsolutePath());
    tng.run();
    Assert.assertTrue(f.exists());
    
    f.deleteOnExit();
  }

  /**
   * @testng.test
   */
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
  
  /**
   * @testng.test
   */
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
  
  /**
   * @testng.test
   */
  public void shouldHonorSuiteName() {
    TestNG testng = TestHelper.createTestNG();
    testng.setTestClasses(new Class[] { A.class, B.class });
    String outputDir = testng.getOutputDirectory();
    String dirA = outputDir + File.separatorChar + "SuiteA-JDK14";
    File fileA = new File(dirA);
    String dirB = outputDir + File.separatorChar + "SuiteB-JDK14";
    File fileB = new File(dirB);
    
    Assert.assertFalse(fileA.exists());
    Assert.assertFalse(fileB.exists());
    testng.run();
    Assert.assertTrue(fileA.exists(), fileA + " wasn't created");
    Assert.assertTrue(fileB.exists(), fileB + " wasn't created");
  }
}
