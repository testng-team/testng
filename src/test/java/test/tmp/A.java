package test.tmp;

import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//@Test(sequential = true)
//@Listeners(C2.class)
public class A {

//  @Factory
  public Object[] f() {
    return new Object[] {
        new A(),
        new A()
    };
  }

//  @BeforeClass(groups = "pre", dependsOnMethods = "bc2")
//  public void bc1() {
//    System.out.println("Before class 1");
//  }
//
//  @BeforeClass(groups = "pre") // , dependsOnMethods = "bc1")
//  public void bc2() {
//    System.out.println("Before class 2");
//  }

//  @AfterMethod
//  public void am() {
//    System.out.println("After method");
//  }

//  @BeforeClass(timeOut = 1000)
  public void bc() throws InterruptedException {
    System.out.println("bc");
    Thread.sleep(2000);
  }

  @Test(timeOut = 1000)
  public void a1() throws InterruptedException {
    Thread.sleep(2000);
//    throw new SkipException("skipped");
  }

  @Test
  public void a2() {
//    throw new RuntimeException();
//    System.out.println("a2 " + Thread.currentThread().getId());
  }
}

