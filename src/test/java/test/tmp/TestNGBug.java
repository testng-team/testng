package test.tmp;

import org.testng.Reporter;
import org.testng.annotations.Test;

public class TestNGBug {
//  @Configuration(beforeTestMethod = true)
  public void init() {
    ppp("Base.init()");
  }

  @Test
  public void test1() {
    Reporter.log("Child.test1");
  }

  @Test(enabled = false)
  public void test2() {
    Reporter.log("Child.test2");
  }

  @Test(groups = "a")
  public void test3() {
    Reporter.log("Child.test3");
  }

  private void ppp(String string) {
    System.out.println("[TestNGBug] " + string);
  }
}
