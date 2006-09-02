package test;


/**
 * This class
 *
 * @author Cedric Beust, May 5, 2004
 * 
 */
public class JUnitTest1 extends BaseTest {
   /**
    * @testng.before-method dependsOnGroups="initTest"
    */
   public void initJUnitFlag() {
      getTest().setJUnit(true);
   }

   /**
    * @testng.test
    */
   public void methodsThatStartWithTest() {
      addClass("test.sample.JUnitSample1");
      assert getTest().isJUnit();

      run();
      String[] passed = {
         "testSample1_1", "testSample1_2"
      };
      String[] failed = {
      };

      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
   }

   /**
    * @testng.test groups="current"
    */
   public void methodsWithSetup() {
      addClass("test.sample.JUnitSample2");
      run();
      String[] passed = {
         "testSample2ThatSetUpWasRun",
      };
      String[] failed = {
      };

      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
   }

   /**
    * @testng.test
    */
   public void testSuite() {
      addClass("test.sample.AllJUnitTests");
      run();
      String[] passed = {
         "testSample1_1", "testSample2ThatSetUpWasRun",
      };
      String[] failed = {
      };

      verifyTests("Passed", passed, getPassedTests());
      verifyTests("Failed", failed, getFailedTests());
   }

   public static void ppp(String s) {
      System.out.println("[JUnitTest1] " + s);
   }


}
