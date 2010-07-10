package test.dependent;

import test.BaseTest;
import org.testng.annotations.*;

public class DependentTest extends BaseTest {
  
  @Test
  public void simpleSkip() {
    addClass("test.dependent.SampleDependent1");
    run();
    String[] passed = {
    };
    String[] failed = {
      "fail"
    };
    String[] skipped = {
      "shouldBeSkipped"
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  public void dependentMethods() {
    addClass("test.dependent.SampleDependentMethods");
    run();
    String[] passed = {
        "oneA", "oneB", "secondA", "thirdA", "canBeRunAnytime"
    };
    String[] failed = {
    };
    String[] skipped = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }
  
  @Test
  public void dependentMethodsWithSkip() {
    addClass("test.dependent.SampleDependentMethods4");
    run();
    String[] passed = {
        "step1",
    };
    String[] failed = {
        "step2",
    };
    String[] skipped = {
        "step3"
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }

  @Test
  @ExpectedExceptions({ org.testng.TestNGException.class })
  public void dependentMethodsWithNonExistentMethod() {
    addClass("test.dependent.SampleDependentMethods5");
    run();
    String[] passed = {
        "step1", "step2"
    };
    String[] failed = {
    };
    String[] skipped = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }
  
  @Test
  @ExpectedExceptions({ org.testng.TestNGException.class })
  public void dependentMethodsWithCycle() {
    addClass("test.dependent.SampleDependentMethods6");
    run();
    String[] passed = {
        "step1", "step2"
    };
    String[] failed = {
    };
    String[] skipped = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
    verifyTests("Skipped", skipped, getSkippedTests());
  }
  
} // DependentTest


