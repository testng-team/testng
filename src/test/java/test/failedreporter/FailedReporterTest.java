package test.failedreporter;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import test.SimpleBaseTest;
import test.failedreporter.issue1297.groups.GroupsPassSample;
import test.failedreporter.issue1297.groups.GroupsSampleBase;
import test.failedreporter.issue1297.inheritance.InheritanceFailureSample;
import test.failedreporter.issue1297.inheritance.InheritancePassSample;
import test.failedreporter.issue1297.straightforward.AllPassSample;
import test.failedreporter.issue1297.straightforward.FailureSample;

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
  public void failedAndSkippedMethodsShouldBeIncluded() {
    testFailedReporter(
        new String[] {"f1", "f2"}, "<include name=\"%s\"/>", FailedReporterSampleTest.class);
  }

  @Test
  public void failedMethodWithDataProviderShouldHaveInvocationNumbers() {
    testFailedReporter(
        new String[] {"f1"},
        "<include name=\"%s\" invocation-numbers=\"1\"/>",
        FailedReporter2SampleTest.class);
  }

  @Test(description = "github-1297")
  public void testExclusionOfPassedConfigs() {
    triggerTest(AllPassSample.class, FailureSample.class);
    String[] substitutions = new String[] {AllPassSample.class.getName()};
    runAssertions(mTempDirectory, substitutions, "<class name=\"%s\">", 0);
    substitutions =
        new String[] {"newTest2", "beforeClassFailureSample", "afterClassFailureSample"};
    runAssertions(mTempDirectory, substitutions, "<include name=\"%s\"/>", 1);
  }

  @Test(description = "github-1297")
  public void testExclusionOfPassedConfigsAmidstInheritance() {
    triggerTest(InheritanceFailureSample.class, InheritancePassSample.class);
    String[] substitutions = new String[] {InheritancePassSample.class.getName()};
    runAssertions(mTempDirectory, substitutions, "<class name=\"%s\">", 0);
    substitutions =
        new String[] {
          "newTest2", "baseBeforeTest", "baseAfterClass", "baseBeforeClass", "baseBeforeMethod"
        };
    runAssertions(mTempDirectory, substitutions, "<include name=\"%s\"/>", 1);
  }

  @Test(description = "github-1297")
  public void testExclusionOfPassedConfigsInvolvingGroupsAtTestLevel() {
    triggerTest(GroupsSampleBase.class.getPackage().getName(), true);
    String[] substitutions = new String[] {GroupsPassSample.class.getName()};
    runAssertions(mTempDirectory, substitutions, "<class name=\"%s\">", 0);
    substitutions = new String[] {"baseBeforeTest", "baseBeforeClassAlwaysRun", "newTest2"};
    runAssertions(mTempDirectory, substitutions, "<include name=\"%s\"/>", 1);
  }

  @Test(description = "github-1297")
  public void testExclusionOfPassedConfigsInvolvingGroupsAtSuiteLevel() {
    triggerTest(GroupsSampleBase.class.getPackage().getName(), false);
    String[] substitutions = new String[] {GroupsPassSample.class.getName()};
    runAssertions(mTempDirectory, substitutions, "<class name=\"%s\">", 0);
    substitutions = new String[] {"baseBeforeTest", "baseBeforeClassAlwaysRun", "newTest2"};
    runAssertions(mTempDirectory, substitutions, "<include name=\"%s\"/>", 1);
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
