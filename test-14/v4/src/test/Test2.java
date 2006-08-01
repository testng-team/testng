package test;




/**
 *
 * @author Cedric Beust, May 5, 2004
 * 
 */
public class Test2 extends BaseTest {
  private boolean m_initializedCorrectly = false;
  private boolean m_initializedIncorrectly = false;

   /**
    * @testng.configuration beforeTestMethod="true"
    */
  public void correctSetup() {
    m_initializedCorrectly = true;
  }
  
  // Shouldn't be called
   /**
    * @testng.configuration beforeTestMethod="true" groups="excludeThisGroup"
    */
  public void incorrectSetup() {
    m_initializedIncorrectly = true;
  }

   /**
    * @testng.test
    */
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

   /**
    * @testng.test
    */
  public void setUpWithGroups() {
    run();
    assert m_initializedCorrectly && (! m_initializedIncorrectly) :
      "Wrong set up method was called, correct:" + m_initializedCorrectly +
      " incorrect:" + m_initializedIncorrectly;
  }

   /**
    * @testng.test
    */
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
  }

   /**
    * @testng.test
    */
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
