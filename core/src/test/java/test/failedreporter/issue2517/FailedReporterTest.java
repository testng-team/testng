package test.failedreporter.issue2517;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;

import java.io.File;
import java.util.List;

public class FailedReporterTest extends SimpleBaseTest {
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
  public void failedMethodWithDataProviderShouldHaveInvocationNumbers() {
    testFailedReporter(
        new String[] {"f1"},
        "<include name=\"%s\" invocation-numbers=\"1\"/>",
        FailedReporter2SampleTest.class);
  }

  private void triggerTest(String packageName, boolean applyGroupSelectionAtTest) {
    final XmlSuite suite = createXmlSuite("1297_suite");

    final XmlTest xmlTest = createXmlTestWithPackages(suite, "1297_test", packageName);
    if (applyGroupSelectionAtTest) {
      createXmlGroups(xmlTest, "run");
    } else {
      createXmlGroups(suite, "run");
    }
    TestNG tng = create(mTempDirectory.toPath(), suite);
    tng.setUseDefaultListeners(true);
    tng.run();
  }

  private void testFailedReporter(String[] expectedMethods, String expectedLine, Class<?>... cls) {
    triggerTest(cls);
    runAssertions(mTempDirectory, expectedMethods, expectedLine);
  }

  private void triggerTest(Class<?>... cls) {
    TestNG tng = create(mTempDirectory.toPath(), cls);
    tng.setUseDefaultListeners(true);
    tng.run();
  }

  static void runAssertions(File outputDir, String[] expectedMethods, String expectedLine) {
    runAssertions(outputDir, expectedMethods, expectedLine, 1);
  }

  private static void runAssertions(
      File outputDir, String[] expectedMethods, String expectedLine, int expected) {
    File failed = new File(outputDir, "testng-failed.xml");
    for (String s : expectedMethods) {
      List<String> resultLines = Lists.newArrayList();
      grep(failed, String.format(expectedLine, s), resultLines);
      Assert.assertEquals(resultLines.size(), expected);
    }
  }
}
