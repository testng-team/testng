package test.failedreporter;

import java.io.File;
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
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import test.SimpleBaseTest;

public class FailedReporterParametersTest extends SimpleBaseTest {
  private File mTempDirectory;

  @BeforeMethod
  public void setUp() {
    mTempDirectory = createDirInTempDir("testng-tmp-" + System.currentTimeMillis() % 1000);
  }

  @AfterMethod
  public void tearDown() {
    deleteDir(mTempDirectory);
  }

  @Test
  public void failedSuiteShouldHaveParameters() {
    Map<String, String> suiteParams = create("suite");
    Map<String, String> testParams = create("test");
    Map<String, String> classParams = create("class");
    Map<String, String> methodParams = create("method");
    // In testng-failed.xml, suite will have both origin suite parameters and children tests
    // parameters.

    XmlSuite suite = createXmlSuite(suiteParams);
    TestNG tng = create(mTempDirectory.toPath(), suite);
    tng.setUseDefaultListeners(true);

    XmlTest test = createXmlTest(suite, suite.getName(), testParams);
    XmlClass clazz = createXmlClass(test, FailedReporterSampleTest.class, classParams);
    createXmlInclude(clazz, "f2", methodParams);
    tng.run();

    runAssertions(
        mTempDirectory,
        "<parameter name=\"%s\" value=\"%s\"/>",
        new String[] {"suiteParam", "testParam", "classParam", "methodParam"});
  }

  private static Map<String, String> create(String prefix) {
    Map<String, String> params = Maps.newHashMap();
    params.put(prefix + "Param", prefix + "ParamValue");
    return params;
  }

  private static void runAssertions(File outputDir, String expectedFormat, String[] expectedKeys) {
    File failed = new File(outputDir, "testng-failed.xml");
    for (String expectedKey : expectedKeys) {
      List<String> resultLines = Lists.newArrayList();
      grep(failed, String.format(expectedFormat, expectedKey, expectedKey + "Value"), resultLines);
      int expectedSize = 1;
      Assert.assertEquals(resultLines.size(), expectedSize, "Mismatch param:" + expectedKey);
    }
  }
}
