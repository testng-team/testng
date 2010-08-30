package test.tmp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

//@Test(sequential = true)
//@Listeners(AListener.class)
public class A extends SimpleBaseTest {

//  @Factory
  public Object[] f() {
    return new Object[] {
        new A(),
        new A()
    };
  }

  @BeforeMethod
  public void bm() {
    System.out.println("Before method");
  }

  @AfterMethod
  public void am() {
    System.out.println("After method");
  }

  @Test
  public void a1() {
    System.out.println("a1 throwing");
    throw new RuntimeException();
//    System.out.println("a1 " + Thread.currentThread().getId());
  }

//  @Test
  public void a2() {
    System.out.println("a2 " + Thread.currentThread().getId());
  }
}
