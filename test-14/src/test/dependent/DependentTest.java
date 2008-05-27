package test.dependent;

import org.testng.TestNG;

import test.BaseTest;

public class DependentTest extends BaseTest {
  
//  /**
//   * @testng.test groups = "current"
//   */
//  public void multiThreadDependencies() {
//    addClass("test.dependent.SampleDependentMethods");
//    setThreadCount(2);
//    setParallel(true);
//    
//    run();
//    
//    String[] passed = {
//        "oneA", "oneB", "secondA", "thirdA", "canBeRunAnytime"
//    };
//    String[] failed = {
//    };
//    
//    verifyTests("Passed", passed, getPassedTests());
//    verifyTests("Failed", failed, getFailedTests());
//  }
  
  /**
   * @testng.test
   */
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

  /**
   * @testng.test
   */
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
  
  /**
   * @testng.test
   */
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

  /**
   * @testng.test
   * @testng.expected-exceptions value="org.testng.TestNGException" 
   */
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
  
  /**
   * @testng.test enabled=false
   * @testng.expected-exceptions value="org.testng.TestNGException" 
   */
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


