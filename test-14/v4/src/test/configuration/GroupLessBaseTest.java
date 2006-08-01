package test.configuration;

import org.testng.Assert;
import org.testng.Reporter;

public class GroupLessBaseTest {

   protected boolean setup;

   /**
    * @testng.configuration beforeSuite = "true"
    */
   public void setUp() {
       setup = true;
   }

   /**
    * @testng.configuration beforeTestClass = "true"
    */
   public void beforeClassMethod() {
   }

   public boolean isSetup() {
       return setup;
   }
   
}