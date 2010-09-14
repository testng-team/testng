package test.tmp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

//@Test(sequential = true)
@Listeners(MyPhaseListener.class)
public class A {

//  @BeforeSuite
  public void bs() {
    System.out.println("bs");
  }

//  @BeforeClass
  public void bc() {
    System.out.println("before");
  }

  @Test(groups="a")
  public void a3() {
    System.out.println("a3");
  }

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

  @Test
  public void a1() {
    System.out.println("a1 throwing");
//    throw new RuntimeException();
//    System.out.println("a1 " + Thread.currentThread().getId());
  }

//  @Test
  public void a2() {
    System.out.println("a2 " + Thread.currentThread().getId());
  }
}
