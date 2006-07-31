package test.parameters;


/**
 * This class
 *
 * @author Cedric Beust, Jul 22, 2004
 * 
 */

public class ParameterSample {
   /**
    * @testng.configuration beforeTestMethod="true" parameters="first-name"
    * @param firstName
    */
   public void beforeTestDeprecated(String firstName) {
//    System.out.println("[ParameterSample] Invoked beforeTestMethod with: " + firstName);
      assert "Cedric".equals(firstName)
            : "Expected Cedric, got " + firstName;
   }

   /**
    * @testng.test groups="singleString" parameters="first-name"
    */
   public void testSingleStringDeprecated(String firstName) {
//    System.out.println("[ParameterSample] Invoked testString " + firstName);
      assert "Cedric".equals(firstName);
   }

   /**
    * @testng.parameters value = "first-name"
    * @testng.configuration beforeTestMethod="true"
    */
   public void beforeTest(String firstName) {
//    System.out.println("[ParameterSample] Invoked beforeTestMethod with: " + firstName);
      assert "Cedric".equals(firstName)
            : "Expected Cedric, got " + firstName;
   }

   /**
    * @testng.parameters value = "first-name"
    * @testng.test groups="singleString"
    */
   public void testSingleString(String firstName) {
//    System.out.println("[ParameterSample] Invoked testString " + firstName);
      assert "Cedric".equals(firstName);
   }   
}
