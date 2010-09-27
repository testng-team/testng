package test.tmp;

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

  @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*foo.*")
  public void a1() {
    throw new RuntimeException("a b c foo d e f");
  }

//  @Test
  public void a2() {
    System.out.println("a2 " + Thread.currentThread().getId());
  }
}
