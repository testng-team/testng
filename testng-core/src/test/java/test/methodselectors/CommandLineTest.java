package test.methodselectors;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.testng.CliTestNgRunner;
import org.testng.ITestResult;
import org.testng.JCommanderCliTestNgRunner;
import org.testng.TestListenerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import testhelper.OutputDirectoryPatch;

public class CommandLineTest extends SimpleBaseTest {

  private final String[] ARG_WITHOUT_CLASSES =
      new String[] {
        "-log", "0", "-d", OutputDirectoryPatch.getOutputDirectory(), "-methodselectors", "", ""
      };

  private final String[] ARG_WITH_GROUPS =
      new String[] {
        "-log", "0",
        "-d", OutputDirectoryPatch.getOutputDirectory(),
        "-testclass", "test.methodselectors.SampleTest",
        "-methodselectors", "",
        "-groups", ""
      };

  private final String[] ARG_WITHOUT_GROUPS =
      new String[] {
        "-log", "0",
        "-d", OutputDirectoryPatch.getOutputDirectory(),
        "-testclass", "test.methodselectors.SampleTest",
        "-methodselectors", "",
      };

  private TestListenerAdapter tla;

  @BeforeMethod
  public void setup() {
    tla = new TestListenerAdapter();
  }

  @Test
  public void commandLineNegativePriorityAllGroups() {
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    ARG_WITHOUT_GROUPS[7] = "test.methodselectors.AllTestsMethodSelector:-1";
    CliTestNgRunner.Main.privateMain(cliRunner, ARG_WITHOUT_GROUPS, tla);
    String[] passed = {"test1", "test2", "test3"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineNegativePriorityGroup2() {
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    ARG_WITHOUT_GROUPS[7] = "test.methodselectors.Test2MethodSelector:-1";
    CliTestNgRunner.Main.privateMain(cliRunner, ARG_WITHOUT_GROUPS, tla);
    String[] passed = {"test2"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineLessThanPriorityTest1Test() {
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    ARG_WITH_GROUPS[7] = "test.methodselectors.Test2MethodSelector:5";
    ARG_WITH_GROUPS[9] = "test1";
    CliTestNgRunner.Main.privateMain(cliRunner, ARG_WITH_GROUPS, tla);
    String[] passed = {"test1", "test2"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineGreaterThanPriorityTest1Test2() {
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    ARG_WITH_GROUPS[7] = "test.methodselectors.Test2MethodSelector:15";
    ARG_WITH_GROUPS[9] = "test1";
    CliTestNgRunner.Main.privateMain(cliRunner, ARG_WITH_GROUPS, tla);
    String[] passed = {"test2"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineLessThanPriorityAllTests() {
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    ARG_WITH_GROUPS[7] = "test.methodselectors.AllTestsMethodSelector:5";
    ARG_WITH_GROUPS[9] = "test1";
    CliTestNgRunner.Main.privateMain(cliRunner, ARG_WITH_GROUPS, tla);
    String[] passed = {"test1", "test2", "test3"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineMultipleSelectors() {
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    ARG_WITH_GROUPS[7] =
        "test.methodselectors.NoTestSelector:7,test.methodselectors.Test2MethodSelector:5";
    ARG_WITH_GROUPS[9] = "test1";
    CliTestNgRunner.Main.privateMain(cliRunner, ARG_WITH_GROUPS, tla);
    String[] passed = {"test1", "test2"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineNoTest1Selector() {
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    ARG_WITHOUT_GROUPS[7] = "test.methodselectors.NoTest1MethodSelector:5";
    CliTestNgRunner.Main.privateMain(cliRunner, ARG_WITHOUT_GROUPS, tla);
    String[] passed = {"test2", "test3"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test
  public void commandLineTestWithXmlFile() {
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    ARG_WITHOUT_CLASSES[5] = "test.methodselectors.NoTest1MethodSelector:5";
    ARG_WITHOUT_CLASSES[6] = getPathToResource("testng-methodselectors.xml");
    CliTestNgRunner.Main.privateMain(cliRunner, ARG_WITHOUT_CLASSES, tla);
    String[] passed = {"test2", "test3"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test(description = "GITHUB-2407")
  public void testOverrideExcludedMethodsCommandLineExclusions() {
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    String[] args =
        new String[] {
          "src/test/resources/test/methodselectors/sampleTest.xml",
          "-log",
          "0",
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          "-excludegroups",
          "test1",
          "-overrideincludedmethods"
        };

    CliTestNgRunner.Main.privateMain(cliRunner, args, tla);

    // test1 is excluded, so only test2 is left in the passed list
    String[] passed = {"test2"};
    String[] failed = {};
    verifyTests("Passed", passed, tla.getPassedTests());
    verifyTests("Failed", failed, tla.getFailedTests());
  }

  @Test(description = "GITHUB-2407")
  public void testOverrideExcludedMethodsSuiteExclusions() {
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    String[] args =
        new String[] {
          "src/test/resources/test/methodselectors/sampleTestExclusions.xml",
          "-log",
          "0",
          "-d",
          OutputDirectoryPatch.getOutputDirectory(),
          "-overrideincludedmethods"
        };

    CliTestNgRunner.Main.privateMain(cliRunner, args, tla);

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
