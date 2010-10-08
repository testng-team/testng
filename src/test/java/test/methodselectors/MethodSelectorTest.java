package test.methodselectors;

import org.testng.annotations.Test;

import test.BaseTest;

public class MethodSelectorTest extends BaseTest {

  @Test
  public void negativePriorityAllGroups() {
    addClass("test.methodselectors.SampleTest");
    addMethodSelector("test.methodselectors.AllTestsMethodSelector", -1);
    run();
    String[] passed = {
        "test1", "test2", "test3",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void negativePriorityGroup2() {
    addClass("test.methodselectors.SampleTest");
    addMethodSelector("test.methodselectors.Test2MethodSelector", -1);
    run();
    String[] passed = {
        "test2",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void lessThanPriorityTest1Test() {
    addClass("test.methodselectors.SampleTest");
    addIncludedGroup("test1");
    addMethodSelector("test.methodselectors.Test2MethodSelector", 5);
    run();
    String[] passed = {
        "test1", "test2",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void greaterThanPriorityTest1Test2() {
    addClass("test.methodselectors.SampleTest");
    addIncludedGroup("test1");
    addMethodSelector("test.methodselectors.Test2MethodSelector", 15);
    run();
    String[] passed = {
        "test2",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void lessThanPriorityAllTests() {
    addClass("test.methodselectors.SampleTest");
    addIncludedGroup("test1");
    addMethodSelector("test.methodselectors.AllTestsMethodSelector", 5);
    run();
    String[] passed = {
        "test1", "test2", "test3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  public static void ppp(String s) {
    System.out.println("[MethodSelectorTest] " + s);
  }
}
