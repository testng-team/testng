package test.failedreporter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.reporters.FailedReporter;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.xml.internal.Parser;
import test.SimpleBaseTest;
import test.reports.SimpleFailedSample;

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

  @Test(description = "github-2008")
  public void preserveParameters() throws IOException {
    XmlSuite xmlSuite = createXmlSuite("Suite");

    XmlTest xmlTest = createXmlTest(xmlSuite, "Test");

    XmlClass xmlClass1 = createXmlClass(xmlTest, SimpleFailedSample.class);
    xmlClass1.getLocalParameters().put("sharedParameter", "44");
    xmlClass1.getLocalParameters().put("class1Parameter", "43");

    XmlClass xmlClass2 = createXmlClass(xmlTest, AnotherSimpleFailedSample.class);
    xmlClass2.getLocalParameters().put("sharedParameter", "55");
    xmlClass2.getLocalParameters().put("class2Parameter", "56");

    TestNG tng = create(xmlSuite);

    Path temp = Files.createTempDirectory("preserveParameters");
    tng.setOutputDirectory(temp.toAbsolutePath().toString());
    tng.addListener(new FailedReporter());
    tng.run();

    Collection<XmlSuite> failedSuites =
        new Parser(temp.resolve(FailedReporter.TESTNG_FAILED_XML).toAbsolutePath().toString())
            .parse();
    XmlSuite failedSuite = failedSuites.iterator().next();
    XmlTest failedTest = failedSuite.getTests().get(0);
    XmlClass failedClass1 =
        failedTest.getClasses().stream()
            .filter(failedClass -> failedClass.getName().equals("test.reports.SimpleFailedSample"))
            .findFirst()
            .get();
    XmlClass failedClass2 =
        failedTest.getClasses().stream()
            .filter(
                failedClass ->
                    failedClass
                        .getName()
                        .equals(
                            "test.failedreporter.FailedReporterParametersTest$AnotherSimpleFailedSample"))
            .findFirst()
            .get();

    // Cheeck class1 Parameters
    Assert.assertEquals("44", failedClass1.getAllParameters().get("sharedParameter"));
    Assert.assertEquals("43", failedClass1.getAllParameters().get("class1Parameter"));
    Assert.assertNull(failedClass1.getAllParameters().get("class2Parameter"));

    // Cheeck class2 Parameters
    Assert.assertEquals("55", failedClass2.getAllParameters().get("sharedParameter"));
    Assert.assertEquals("56", failedClass2.getAllParameters().get("class2Parameter"));
    Assert.assertNull(failedClass2.getAllParameters().get("class1Parameter"));
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

  class AnotherSimpleFailedSample {

    @Test
    public void failed() {
      throw new RuntimeException("Failing intentionally");
    }
  }
}
