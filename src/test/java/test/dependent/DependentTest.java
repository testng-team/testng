package test.dependent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.xml.XmlSuite.ParallelMode;
import test.BaseTest;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;
import test.dependent.github1156.ASample;
import test.dependent.github1156.BSample;
import test.dependent.github1380.GitHub1380Sample;
import test.dependent.github1380.GitHub1380Sample2;
import test.dependent.github1380.GitHub1380Sample3;
import test.dependent.github1380.GitHub1380Sample4;

public class DependentTest extends BaseTest {

  @Test
  public void simpleSkip() {
    addClass(SampleDependent1.class.getName());
    run();
    String[] passed = {};
    String[] failed = {"fail"};
    String[] skipped = {"shouldBeSkipped"};
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void dependentMethods() {
    addClass(SampleDependentMethods.class.getName());
    run();
    String[] passed = {"oneA", "oneB", "secondA", "thirdA", "canBeRunAnytime"};
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
    String[] passed = {
      "step1",
    };
    String[] failed = {
      "step2",
    };
    String[] skipped = {"step3"};
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test(expectedExceptions = {org.testng.TestNGException.class})
  public void dependentMethodsWithNonExistentMethod() {
    addClass(SampleDependentMethods5.class.getName());
    run();
    String[] passed = {"step1", "step2"};
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
    String[] passed = {
      "init",
    };
    String[] failed = {
      "fail",
    };
    String[] skipped = {"skip1", "skip2"};
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void instanceDependencies() {
    addClass(InstanceSkipSampleTest.class.getName());
    run();
    verifyInstanceNames("Passed", getPassedTests(), new String[] {"f#1", "f#3", "g#1", "g#3"});
    verifyInstanceNames("Failed", getFailedTests(), new String[] {"f#2"});
    verifyInstanceNames("Skipped", getSkippedTests(), new String[] {"g#2"});
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

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {
      {new Class[] {ASample.class, BSample.class}, true},
      {new Class[] {ASample.class, BSample.class}, false},
      {new Class[] {BSample.class, ASample.class}, true},
      {new Class[] {BSample.class, ASample.class}, false}
    };
  }

  @Test(dataProvider = "dp", description = "GITHUB-1156")
  public void methodDependencyBetweenClassesShouldWork(Class[] classes, boolean preserveOrder) {
    TestNG tng = SimpleBaseTest.create(classes);
    tng.setPreserveOrder(preserveOrder);

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly("testB", "testA");
  }

  @DataProvider
  public static Object[][] dp1380() {
    return new Object[][] {
      {GitHub1380Sample.class, new String[] {"testMethodA", "testMethodB", "testMethodC"}, false},
      {GitHub1380Sample2.class, new String[] {"testMethodC", "testMethodB", "testMethodA"}, false},
      {GitHub1380Sample3.class, new String[] {"testMethodA", "testMethodB", "testMethodC"}, false},
      {GitHub1380Sample4.class, new String[] {"testMethodB", "testMethodA", "testMethodC"}, false},
      {GitHub1380Sample.class, new String[] {"testMethodA", "testMethodB", "testMethodC"}, true},
      {GitHub1380Sample2.class, new String[] {"testMethodC", "testMethodB", "testMethodA"}, true},
      {GitHub1380Sample3.class, new String[] {"testMethodA", "testMethodB", "testMethodC"}, true},
      {GitHub1380Sample4.class, new String[] {"testMethodB", "testMethodA", "testMethodC"}, true}
    };
  }

  @Test(dataProvider = "dp1380", description = "GITHUB-1380")
  public void simpleCyclingDependencyShouldWork(
      Class<?> testClass, String[] runMethods, boolean isParallel) {
    TestNG tng = SimpleBaseTest.create(testClass);
    if (isParallel) {
      tng.setParallel(ParallelMode.METHODS);
    }

    InvokedMethodNameListener listener = new InvokedMethodNameListener();
    tng.addListener(listener);

    tng.run();

    assertThat(listener.getSucceedMethodNames()).containsExactly(runMethods);
  }
}
