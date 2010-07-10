package test;

import org.testng.annotations.*;

public class MethodTest extends BaseTest {
  private static final String CLASS_NAME = "test.sample.Sample2";

  @Test(groups = { "current" })
  public void includeMethodsOnly() {
    addClass(CLASS_NAME);
    assert 1 == getTest().getXmlClasses().size();
    addIncludedMethod(CLASS_NAME, ".*method2");
    run();
    String[] passed = {
      "method2",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }
  
  @Test(groups = { "current" })
  public void excludeMethodsOnly() {
    addClass(CLASS_NAME);
    assert 1 == getTest().getXmlClasses().size();
    addExcludedMethod(CLASS_NAME, ".*method2");
    run();
    String[] passed = {
      "method1", "method3"
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }
  
  @Test
  public void excludePackage() {
    addClass(CLASS_NAME);
    assert 1 == getTest().getXmlClasses().size();
    addExcludedMethod(CLASS_NAME, ".*");
    run();
    String[] passed = {
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }
}