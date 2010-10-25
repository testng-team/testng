package test.methodselectors;

import org.testng.annotations.Test;

import test.BaseTest;

public class BeanShellTest extends BaseTest {

  @Test
  public void onlyGroup1() {
    addClass("test.methodselectors.SampleTest");
    setBeanShellExpression("groups.\n     containsKey   \t    (\"test1\")");
    run();
    String[] passed = {
        "test1",
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
