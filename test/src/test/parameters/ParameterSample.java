package test.parameters;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * This class
 *
 * @author Cedric Beust, Jul 22, 2004
 * 
 */

public class ParameterSample {
  
  @Parameters({ "first-name" })
  @BeforeMethod
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
