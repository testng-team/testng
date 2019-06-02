package test.listeners.invokeasinsertionorder;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SimpleTestClassWithConfigMethod {
    
  @BeforeMethod
  public void beforeMethod() {}

  @Test(priority = 0)
  public void testWillPass() {}
  
  @Test(priority = 1)
  public void testWillFail() {
      Assert.fail();
  } 
  
  @AfterClass
  public void afterClass() {
     Assert.fail();
  }
  
  @AfterTest
  public void afterTest() {}
}
