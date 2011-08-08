package test.dependent;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

import test.BaseTest;
import test.SimpleBaseTest;

import java.util.List;

public class DependentTest extends BaseTest {

  @Test
  public void simpleSkip() {
    addClass(SampleDependent1.class.getName());
    run();
    String[] passed = {};
    String[] failed = { "fail" };
    String[] skipped = { "shouldBeSkipped" };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void dependentMethods() {
    addClass(SampleDependentMethods.class.getName());
    run();
    String[] passed = { "oneA", "oneB", "secondA", "thirdA", "canBeRunAnytime" };
    String[] failed = {};
    String[] skipped = {};
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void dependentMethodsWithSkip() {
    addClass(SampleDependentMethods4.class.getName());
    run();
    String[] passed = { "step1", };
    String[] failed = { "step2", };
    String[] skipped = { "step3" };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  @ExpectedExceptions({ org.testng.TestNGException.class })
  public void dependentMethodsWithNonExistentMethod() {
    addClass(SampleDependentMethods5.class.getName());
    run();
    String[] passed = { "step1", "step2" };
    String[] failed = {};
    String[] skipped = {};
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test(expectedExceptions = org.testng.TestNGException.class)
  public void dependentMethodsWithCycle() {
    addClass(SampleDependentMethods6.class.getName());
    run();
  }

  @Test(expectedExceptions = org.testng.TestNGException.class)
  public void dependentGroupsWithCycle() {
    addClass("test.dependent.SampleDependentMethods7");
    run();
  }

  @Test
  public void multipleSkips() {
    addClass(MultipleDependentSampleTest.class.getName());
    run();
    String[] passed = { "init", };
    String[] failed = { "fail", };
    String[] skipped = { "skip1", "skip2" };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void instanceDependencies() {
    addClass(InstanceSkipSampleTest.class.getName());
    run();
    verifyInstanceNames("Passed", getPassedTests(),
        new String[] { "f#1", "f#3", "g#1", "g#3"});
    verifyInstanceNames("Failed", getFailedTests(),
        new String[] { "f#2" });
    verifyInstanceNames("Skipped", getSkippedTests(),
        new String[] { "g#" });
  }

  @Test
  public void dependentWithDataProvider() {
    TestNG tng = SimpleBaseTest.create(DependentWithDataProviderSampleTest.class);
    tng.setGroupByInstances(true);
    List<String> log = DependentWithDataProviderSampleTest.m_log;
    log.clear();
    tng.run();
    for (int i = 0; i < 12; i += 4) {
      String[] s = log.get(i).split("#");
      String instance = s[1];
      Assert.assertEquals(log.get(i), "prepare#" + instance);
      Assert.assertEquals(log.get(i + 1), "test1#" + instance);
      Assert.assertEquals(log.get(i + 2), "test2#" + instance);
      Assert.assertEquals(log.get(i + 3), "clean#" + instance);
    }
  }
} // DependentTest

