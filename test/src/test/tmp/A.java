package test.tmp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class A {
  @DataProvider(name = "create")
  public Object[][] create(Integer n) {
    return new Object[][] {
        new Object[] {}
    };
  }
  
  @Test(dataProvider = "create")
  public void f(Object o) {
    
  }
  @BeforeMethod
  public void setUp() {
    ppp("SETUP THREAD: " + Thread.currentThread().getId());
  }

  @AfterMethod
  public void tearDown() {
    ppp("TEARDOWN THREAD: " + Thread.currentThread().getId());
  }
  
  @Test(invocationCount = 5)
  public void test() {
    ppp("TEST THREAD: " + Thread.currentThread().getId());    
  }

  private void ppp(String string) {
    System.out.println("[A] " + string);
  }
  

}
