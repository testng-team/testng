package test.tmp;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class A {
  
  @AfterTest(alwaysRun = true)
  public void at() {
    ppp("AFTER TEST");
  }
  
  @Test
  public void f() {
    throw new RuntimeException();
  }
//   @Test(groups = "foo")
//   public void a() {
//       System.out.println( "a" );
//   }
  
  

  private static void ppp(String s) {
    System.out.println("[A] " + s);
  }
}