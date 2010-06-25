package test.tmp;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

@Listeners(BListener.class)
public class B extends SimpleBaseTest {

  @BeforeMethod
  public void bm() {
    System.out.println("Before");
  }

  @Test
  public void f() {
    System.out.println("f()");
  }

  @AfterMethod
  public void am() {
    System.out.println("After");
  }
}

