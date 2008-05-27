package example1;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ExpectedExceptions;
import org.testng.annotations.Test;

/**
 * This class
 *
 * @author Cedric Beust, Apr 26, 2004
 * 
 */

@Test(groups = { "functest" }, enabled = true )
public class Test1 {
  
  @BeforeClass
  public static void setupClass() {
    ppp("SETTING UP THE CLASS");
  }

  @AfterClass
  public static void tearDownClass1() {
    ppp("TEARING DOWN THE CLASS PART 1");
  }

  @AfterClass
  public static void tearDownClass2() {
    ppp("TEARING DOWN THE CLASS PART 2");
  }

  @BeforeMethod
  public void beforeTestMethod() {
    ppp("BEFORE METHOD");  
  }
  
  @AfterMethod
  public void afterTestMethod() {
    ppp("AFTER METHOD");  
  }

  @Test(groups = { "odd" })
  public void testMethod1() {
    ppp(".....  TESTING1");
  }
  
  @Test(groups = {"even"} )
  public void testMethod2() {
    ppp(".....  TESTING2");
  }
  
  @Test(groups = { "odd" })
  public void testMethod3() {
    ppp(".....  TESTING3");
  }

  @Test(groups = { "odd" }, enabled = false)
  public void testMethod5() {
    ppp(".....  TESTING5");
  }
  
  @Test(groups = { "broken" })
  public void testBroken() {
    ppp(".....  TEST BROKEN");
  }
  
  @Test(groups = { "fail" }, expectedExceptions =
      { NumberFormatException.class, ArithmeticException.class } )
  public void throwExpectedException1ShouldPass() {
    throw new NumberFormatException();
  }

  @Test(groups = { "fail" }, expectedExceptions = 
    { NumberFormatException.class, ArithmeticException.class } )
  public void throwExpectedException2ShouldPass() {
    throw new ArithmeticException();
  }
  
  private static void ppp(String s) {
  System.out.println("[Test1] " + s);
}
}
