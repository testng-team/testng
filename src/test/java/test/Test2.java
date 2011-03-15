package test;

import org.testng.Assert;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;


/**
 *
 * @author Cedric Beust, May 5, 2004
 *
 */
public class Test2 extends BaseTest {
  private boolean m_initializedCorrectly = false;

  @Configuration(beforeTestMethod = true)
//  @BeforeMethod
  public void correctSetup() {
    m_initializedCorrectly = true;
  }

  // Shouldn't be called
  @Configuration(beforeTestMethod = true, groups = "excludeThisGroup")
//  @BeforeMethod(groups = { "excludeThisGroup"} )
  public void incorrectSetup() {
    throw new RuntimeException("Should never be run");
  }

  @Test
  public void noGroups() {
    addClass("test.sample.Sample1");
    run();
    String[] passed = {
      "method1",
      "method2", "method3",
      "broken", "throwExpectedException1ShouldPass",
      "throwExpectedException2ShouldPass"
    };
    String[] failed = {
        "throwExceptionShouldFail", "verifyLastNameShouldFail"
    };
    verifyTests("Passed", passed, getPassedTests());
    verifyTests("Failed", failed, getFailedTests());
  }

  @Test
  public void setUpWithGroups() {
    run();
    Assert.assertTrue(m_initializedCorrectly, "Should have run the correctSetup method");
  }

  @Test
  public void partialGroupsClass() {
    addClass("test.sample.PartialGroupTest");
    addIncludedGroup("classGroup");
    run();
    String[] passed = {
        "testMethodGroup", "testClassGroup"
      };
      String[] failed = {
          "testMethodGroupShouldFail", "testClassGroupShouldFail"
      };
      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());

//      ppp("@@@@@@@@@@@@@@@@@@ PASSED TESTS");
//      for (Object o : getPassedTests().values()) {
//        ppp("@@@@@@@@@@ PASSED:" + o);
//      }
  }

  @Test
  public void partialGroupsMethod() {
    addClass("test.sample.PartialGroupTest");
    addIncludedGroup("methodGroup");
    run();
    String[] passed = {
        "testMethodGroup",
      };
      String[] failed = {
         "testMethodGroupShouldFail"
      };
      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
  }

}
