package test.methodselectors;

import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import testhelper.OutputDirectoryPatch;

public class CommandLineTest extends SimpleBaseTest {

  @Test
  public void multipleSelectors() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class<?>[] {test.methodselectors.SampleTest.class});
    testng.addMethodSelector("test.methodselectors.NoTestSelector", 7);
    testng.addMethodSelector("test.methodselectors.Test2MethodSelector", 5);
    testng.setGroups("test1");
    testng.run();

    String[] passed = {"test1", "test2"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineNoTest1Selector() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setTestClasses(new Class<?>[] {test.methodselectors.SampleTest.class});
    testng.addMethodSelector("test.methodselectors.NoTest1MethodSelector", 5);
    testng.run();

    String[] passed = {"test2", "test3"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineTestWithXmlFile() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.addMethodSelector("test.methodselectors.NoTest1MethodSelector", 5);
    testng.setTestSuites(
        Collections.singletonList(getPathToResource("testng-methodselectors.xml")));
    testng.run();

    String[] passed = {"test2", "test3"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test(description = "GITHUB-2407")
  public void testOverrideExcludedMethodsCommandLineExclusions() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setTestSuites(
        Collections.singletonList(getPathToResource("test/methodselectors/sampleTest.xml")));
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setExcludedGroups("test1");
    testng.setOverrideIncludedMethods(true);
    testng.run();

    // test1 is excluded, so only test2 is left in the passed list
    String[] passed = {"test2"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test(description = "GITHUB-2407")
  public void testOverrideExcludedMethodsSuiteExclusions() {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = new TestNG();
    testng.addListener(tla);
    testng.setTestSuites(
        Collections.singletonList(
            getPathToResource("test/methodselectors/sampleTestExclusions.xm")));
    testng.setVerbose(0);
    testng.setOutputDirectory(OutputDirectoryPatch.getOutputDirectory());
    testng.setOverrideIncludedMethods(true);
    testng.run();

    String[] passed = {};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  private void verifyTests(String title, String[] expected, List<ITestResult> found) {

    Assertions.assertThat(found.stream().map(ITestResult::getName).toArray(String[]::new))
        .describedAs(title)
        .isEqualTo(expected);
  }
}
