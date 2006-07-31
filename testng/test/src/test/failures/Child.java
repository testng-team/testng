package test.failures;

import org.testng.annotations.Test;

public class Child extends Base1 {

  @Test
  public void pass() {
      assert true;
  }

  @Test
  public void fail() {
    throw new RuntimeException("VOLUNTARILY FAILED");
  }
}
