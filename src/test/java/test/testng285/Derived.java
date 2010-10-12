package test.testng285;

import org.testng.annotations.Test;

public class Derived extends BugBase {

  @Test
  public void f1() {
    log(Thread.currentThread().getId());
  }

  @Test
  public void f2() {
    log(Thread.currentThread().getId());
  }
}
