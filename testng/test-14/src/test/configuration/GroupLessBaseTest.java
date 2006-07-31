package test.configuration;

public class GroupLessBaseTest {

   protected boolean setup;

   /**
    * @testng.before-suite
    */
   public void setUp() {
       setup = true;
   }

   /**
    * @testng.before-class
    */
   public void beforeClassMethod() {
   }

   public boolean isSetup() {
       return setup;
   }
   
}