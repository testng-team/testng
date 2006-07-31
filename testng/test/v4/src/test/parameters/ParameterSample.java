package test.parameters;

import org.testng.annotations.*;

/**
 * This class
 *
 * @author Cedric Beust, Jul 22, 2004
 * 
 */

public class ParameterSample {
  
  @Configuration(beforeTestMethod = true, parameters = "first-name")
  public void beforeTestDeprecated(String firstName) {
//    System.out.println("[ParameterSample] Invoked beforeTestMethod with: " + firstName);
    assert "Cedric".equals(firstName)
     : "Expected Cedric, got " + firstName;
  }
    
  
  @Test(groups = { "singleString"}, parameters = { "first-name" })
  public void testSingleStringDeprecated(String firstName) {
//    System.out.println("[ParameterSample] Invoked testString " + firstName);
    assert "Cedric".equals(firstName);
  }

  @Parameters({ "first-name" })
  @Configuration(beforeTestMethod = true)
  public void beforeTest(String firstName) {
//    System.out.println("[ParameterSample] Invoked beforeTestMethod with: " + firstName);
    assert "Cedric".equals(firstName)
     : "Expected Cedric, got " + firstName;
  }
    
  
  @Parameters({ "first-name" })
  @Test(groups = { "singleString"})
  public void testSingleString(String firstName) {
//    System.out.println("[ParameterSample] Invoked testString " + firstName);
    assert "Cedric".equals(firstName);
  }
  
}
