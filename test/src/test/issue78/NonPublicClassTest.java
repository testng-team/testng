package test.issue78;

import org.testng.TestNGException;
import org.testng.annotations.Test;

import test.BaseTest;

public class NonPublicClassTest extends BaseTest
{
  @Test(expectedExceptions = {TestNGException.class}, 
        expectedExceptionsMessageRegExp = "\\nAn error occurred while instantiating " +
        		"class ([\\w|\\.]*?)\\. Check to make sure it can be accessed/instantiated\\.")
  public void dontThrowNPEForNonPublicTestClass() {
    addClass(NonPublicClass.class.getName());
    run();
  }
}

class NonPublicClass {

  @Test
  public void testInNonPublicClass() {}
}
