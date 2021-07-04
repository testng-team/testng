package test.mixed;

import org.testng.Assert;
import org.testng.CliTestNgRunner;
import org.testng.JCommanderCliTestNgRunner;
import org.testng.TestListenerAdapter;
import org.testng.annotations.Test;
import test.BaseTest;
import testhelper.OutputDirectoryPatch;

/** @author lukas */
public class MixedTest extends BaseTest {
  @Test
  public void mixedWithExcludedGroups() {
    String[] argv = {
      "-d",
      OutputDirectoryPatch.getOutputDirectory(),
      "-log",
      "0",
      "-mixed",
      "-groups",
      "unit",
      "-excludegroups",
      "ignore",
      "-testclass",
      "test.mixed.JUnit3Test1,test.mixed.JUnit4Test1,test.mixed.TestNGTest1,test.mixed.TestNGGroups"
    };
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    TestListenerAdapter tla = new TestListenerAdapter();
    CliTestNgRunner.Main.privateMain(cliRunner, argv, tla);

    Assert.assertEquals(
        tla.getPassedTests().size(),
        5); // 2 from junit3test1, 2 from junit4test1, 0 from testngtest1 (no groups), 1 from
    // testnggroups (1 is included, 1 is excluded)
    Assert.assertEquals(tla.getFailedTests().size(), 0);
  }

  @Test
  public void mixedClasses() {
    String[] argv = {
      "-d",
      OutputDirectoryPatch.getOutputDirectory(),
      "-log",
      "0",
      "-mixed",
      "-testclass",
      "test.mixed.JUnit3Test1,test.mixed.JUnit4Test1,test.mixed.TestNGTest1"
    };
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    TestListenerAdapter tla = new TestListenerAdapter();
    CliTestNgRunner.Main.privateMain(cliRunner, argv, tla);

    Assert.assertEquals(tla.getPassedTests().size(), 6);
    Assert.assertEquals(tla.getFailedTests().size(), 0);
  }

  @Test
  public void mixedMethods() {
    String[] argv = {
      "-d",
      OutputDirectoryPatch.getOutputDirectory(),
      "-mixed",
      "-log",
      "0",
      "-methods",
      "test.mixed.JUnit3Test1.testB,test.mixed.JUnit4Test1.atest,test.mixed.TestNGTest1.tngCustomTest1"
    };
    CliTestNgRunner cliRunner = new JCommanderCliTestNgRunner();
    TestListenerAdapter tla = new TestListenerAdapter();
    CliTestNgRunner.Main.privateMain(cliRunner, argv, tla);

    Assert.assertEquals(tla.getPassedTests().size(), 3);
    Assert.assertEquals(tla.getFailedTests().size(), 0);
  }
}
