package test.privatemethod;

import org.testng.Assert;


public class PrivateMethodTest {
  public PrivateMethodTest(String name, int value) {
  }
  
  private int privateMethod() {
    return 1;
  }
  
  public static class PrivateMethodInnerTest {
    /**
     * @testng.test
     */
    public void testPrivateMethod() {
      PrivateMethodTest pmt = new PrivateMethodTest("aname", 1);
      int returnValue = pmt.privateMethod();
      
      Assert.assertEquals(returnValue, 1);
    }
  }
}
