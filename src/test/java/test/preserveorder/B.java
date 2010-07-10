package test.preserveorder;

import org.testng.annotations.Test;

import test.BaseLogTest;

public class B extends BaseLogTest {

  @Test
  public void b1() {
    log("B.b1");
  }

  @Test
  public void b2() {
    log("B.b2");
  }

  @Test
  public void b3() {
    log("B.b3");
  }
}
