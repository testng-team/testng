package test.failedreporter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.BaseTest;

public class FailedReporterParametersTest extends BaseTest {
  private File mTempDirectory;

  @BeforeMethod
  public void setUp() {
    File slashTmpDir = new File(System.getProperty("java.io.tmpdir"));
    mTempDirectory = new File(slashTmpDir, "testng-tmp-" + System.currentTimeMillis() % 1000);
    mTempDirectory.mkdirs();
    mTempDirectory.deleteOnExit();
  }

  @AfterMethod
  public void tearDown() {
    deleteDir(mTempDirectory);
  }

  @Test
  public void failedSuiteShouldHaveParameters() throws IOException {
    Map<String, String> suiteParams = Maps.newHashMap();
    Map<String, String> testParams = Maps.newHashMap();
    Map<String, String> classParams = Maps.newHashMap();
    Map<String, String> methodParams = Maps.newHashMap();
    suiteParams.put("suiteParam", "suiteParamValue");
    //In testng-failed.xml, suite will have both origin suite parameters and children tests parameters.
    testParams.put("testParam", "testParamValue");
    classParams.put("classParam", "classParamValue");
    methodParams.put("methodParam", "methodParamValue");  
    
    TestNG tng = new TestNG();    
    XmlSuite suite = new XmlSuite();
    suite.setParameters(suiteParams);    
    tng.setXmlSuites(Lists.newArrayList(suite));
    XmlTest test = new XmlTest(suite);
    XmlClass clazz = new XmlClass();
    XmlInclude includeMethod = new XmlInclude("f2");
    test.setParameters(testParams);
    test.setXmlClasses(Lists.newArrayList(clazz));    
    clazz.setParameters(classParams);
    clazz.setXmlTest(test); 
    clazz.setIncludedMethods(Lists.newArrayList(includeMethod));
    clazz.setClass(FailedReporterSampleTest.class);    
    includeMethod.setParameters(methodParams);
    includeMethod.setXmlClass(clazz);
    tng.setVerbose(0);   
    tng.setOutputDirectory(mTempDirectory.getAbsolutePath());
    tng.run();
    
    runAssertions(mTempDirectory, "<parameter name=\"%s\" value=\"%s\"/>", 
        new String[] {"suiteParam", "testParam", "classParam", "methodParam"});
    
  }

  public static void runAssertions(File outputDir, String expectedFormat, String[] expectedKeys) {
    File failed = new File(outputDir, "testng-failed.xml");
    for (String expectedKey : expectedKeys) {    	
      List<String> resultLines = Lists.newArrayList();
      grep(failed, String.format(expectedFormat, expectedKey, expectedKey + "Value"), resultLines);
      int expectedSize = 1;
      if ("testParam".equals(expectedKey)) {
        expectedSize = 2;
      }
      Assert.assertEquals(resultLines.size(), expectedSize, "Mismatch param:" + expectedKey);
    }
  }
}
