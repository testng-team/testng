package test.tmp.verify;

import org.testng.annotations.Test;

public class VerifyTest {

  @Test
  public void f2() {
    System.out.println("f2()");
  }

  @Test
  public void f1() {
    System.out.println("f1()");
  }

  @Verify
  public void verify() {
//    throw new RuntimeException();
//    System.out.println("verify()");
  }
}
