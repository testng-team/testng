package example1;

import org.testng.annotations.Configuration;

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
  
  @Configuration(beforeTestClass = true)
  public static void setupClass() {
    ppp("SETTING UP THE CLASS");
  }

  @Configuration(afterTestClass = true)
  public static void tearDownClass1() {
    ppp("TEARING DOWN THE CLASS PART 1");
  }

  @Configuration(afterTestClass = true)
  public static void tearDownClass2() {
    ppp("TEARING DOWN THE CLASS PART 2");
  }

  @Configuration(beforeTestMethod = true)
  public void beforeTestMethod() {
    ppp("BEFORE METHOD");  
  }
  
  @Configuration(afterTestMethod = true)
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
  
  @Test(groups = { "fail" })
  @ExpectedExceptions( { NumberFormatException.class, ArithmeticException.class } )
  public void throwExpectedException1ShouldPass() {
    throw new NumberFormatException();
  }

  @Test(groups = { "fail" })
  @ExpectedExceptions( { NumberFormatException.class, ArithmeticException.class } )
  public void throwExpectedException2ShouldPass() {
    throw new ArithmeticException();
  }
  
  private static void ppp(String s) {
  System.out.println("[Test1] " + s);
}
}
