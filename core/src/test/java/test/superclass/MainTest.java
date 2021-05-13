package test.superclass;

import org.testng.annotations.Test;

import test.BaseTest;

public class MainTest extends BaseTest {

  @Test
  public void baseMethodIsCalledWithMethodTest() {
    addClass("test.superclass.Child1Test");
    run();
    String[] passed = {
      "tbase", "t1", "t2", "t3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void baseMethodIsCalledWithClassTest() {
    addClass("test.superclass.Child2Test");
    run();
    String[] passed = {
      "tbase", "t1", "t2", "t3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }


}
