package test.parameters;

import org.testng.annotations.Test;

import test.BaseTest;

/**
 * This class
 *
 * @author Cedric Beust, Jul 22, 2004
 *
 */
public class ParameterTest extends BaseTest {

  public static void ppp(String s) {
    System.out.println("[ParameterTest] " + s);
  }

  @Test
  public void stringSingle() {
    addClass("test.parameters.ParameterSample");
    setParameter("first-name", "Cedric");
    run();
    String[] passed = {
      "testSingleString",
      "testNonExistentParameter",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void beforeMethodWithParameters() {
    addClass("test.parameters.BeforeSampleTest");
    setParameter("parameter", "parameter value");
    run();
    String[] passed = {
      "testExample",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

}
