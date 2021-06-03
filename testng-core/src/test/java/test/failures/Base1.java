package test.failures;

import org.testng.annotations.Test;

public class Base1 extends Base0 {

  @Test
  public void base2() {}

  @Test
  public void failFromBase() {
    throw new RuntimeException("VOLUNTARILY FAILED");
  }
}
