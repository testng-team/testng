package test.tmp;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.SkipException;
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
//  public void bc() throws InterruptedException {
//    System.out.println("bc");
//    Thread.sleep(2000);
//  }

//  @Test(timeOut = 1000)
//  public void a1() throws InterruptedException {
//    Thread.sleep(2000);
//    throw new SkipException("skipped");
//  }

//  @BeforeMethod
  public void bm() {
    throw new RuntimeException("Ex");
//    System.out.println("@BeforeMethod");
  }

  @Test
  public void a1(ITestContext context) {
//    throw new RuntimeException();
    System.out.println("Context:" + context.getSuite().getXmlSuite().getFileName());
    Assert.assertTrue(true);
//    System.out.println("test1");
  }

  @Test(dependsOnMethods = "a1")
  public void a2() {
//    System.out.println("test2");
//    throw new RuntimeException("We have a problem");
//    System.out.println("a2 " + Thread.currentThread().getId());
  }

  @Test(enabled = false, description = "This test is disabled")
  public void a3() {
    throw new SkipException("Skipped");
  }
}

