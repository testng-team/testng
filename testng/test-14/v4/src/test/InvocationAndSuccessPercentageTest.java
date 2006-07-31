package test;


/**
 * This class tests invocationCount and successPercentage
 * 
 * @author cbeust
 */
public class InvocationAndSuccessPercentageTest extends BaseTest {

  /**
   * @testng.test
   */
    public void invocationCount() {
      addClass("test.sample.InvocationCountTest");
      addIncludedGroup("invocationOnly");
      run();
      String[] passed = {
          "tenTimesShouldSucceed",
      };
      String[] failed = {
      };
      verifyResults(getPassedTests(), 10, "passed tests");
//      Map passedTests = getPassedTests();
//      Set keys = passedTests.keySet();
//      Object firstKey = keys.iterator().next();
//      ITestResult passedResult = (ITestResult) passedTests.get(firstKey);
//      int n = passedResult.getPassedMethods().size();
//      assert n == 10 :
//        "Expected 10 tests to have passed, but only found " + n;
      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
    }
   
   /**
    * @testng.test
    * 
    * Result expected:
    * 8 passed
    * 2 failed but within success percentage
    */
   public void successPercentageThatSucceeds() {
     addClass("test.sample.InvocationCountTest");
     addIncludedGroup("successPercentageThatSucceedsOnly");
     run();
     String[] passed = {
         "successPercentageShouldSucceed",
     };
     String[] failed = {
     };
     String[] failedButWithinSuccessPercentage = {
         "successPercentageShouldSucceed",         
     };
     verifyTests("FailedButWithinSuccessPercentage", 
         failedButWithinSuccessPercentage, getFailedButWithinSuccessPercentageTests());
     verifyTests("Passed", passed, getPassedTests());
     verifyTests("Failed", failed, getFailedTests());
     
     // Should have 8 passed, 2 failed but within success percentage
     verifyResults(getPassedTests(), 8, "passed tests");
     verifyResults(
       getFailedButWithinSuccessPercentageTests(), 2,
       "failed_but_within_success_percentage_tests");
   }

   /**
    * @testng.test
    * 
    * Result expected:
    * 8 passed
    * 1 failed but within success percentage
    * 1 failed
    */
   public void successPercentageThatFails() {
     addClass("test.sample.InvocationCountTest");
     addIncludedGroup("successPercentageThatFailsOnly");
     run();
     String[] passed = {
         "successPercentageShouldFail",
     };
     String[] failed = {
         "successPercentageShouldFail",         
     };
     String[] failedButWithinSuccessPercentage = {
         "successPercentageShouldFail",         
     };
     verifyTests("FailedButWithinSuccessPercentage", 
         failedButWithinSuccessPercentage, getFailedButWithinSuccessPercentageTests());
     verifyTests("Passed", passed, getPassedTests());
     verifyTests("Failed", failed, getFailedTests());
     
     // Should have 8 passed, 2 failed but within success percentage
     verifyResults(getPassedTests(), 8, "passed tests");
     verifyResults(getFailedTests(), 1, "failed tests");
     verifyResults(
       getFailedButWithinSuccessPercentageTests(), 1,
       "failed_but_within_success_percentage_tests");
   }   

   public static void ppp(String s) {
    System.out.println("[Invocation] " + s);
  }
  
}
