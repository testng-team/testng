package test.parameters;

import org.testng.annotations.*;

import test.BaseTest;

/**
 * This class
 *
 * @author Cedric Beust, Jul 22, 2004
 * 
 */
public class ParameterTest extends BaseTest {
  
//  @Configuration(beforeTestMethod = true)
//  public void methodSetUp() {
//    m_testRunner.setClasses(new Class[] { test.ParameterTest.class });    
//  }
  
  public static void ppp(String s) {
    System.out.println("[ParameterTest] " + s);
  }
  
  @Test
  public void stringSingle() {
    addClass("test.parameters.ParameterSample");
    setParameter("first-name", "Cedric");
    addIncludedGroup("singleString");
    run();
    String[] passed = {
      "testSingleString",
      "testSingleStringDeprecated",
    };
    String[] failed = {
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }


}
