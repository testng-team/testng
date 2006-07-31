package test.configuration;

import org.testng.Assert;
import org.testng.Reporter;

public class GroupLessTest extends GroupLessBaseTest {

   /**
    * @testng.test groups = "init"
    */
   public void initMethod() {
     Assert.assertTrue(setup);
   }

   /**
    * @testng.test groups = "init"
    * dependsOnMethods = "initMethod"
    */
   public void startMethod() {
   }

   /**
    * @testng.test groups = "run"
    * dependsOnGroups = "init"
    */
   public void endMethod() {
   }

   /**
    * @testng.test dependsOnMethods = "endMethod"
    */
   public void verify() {
     Assert.assertTrue(setup);
   }


}