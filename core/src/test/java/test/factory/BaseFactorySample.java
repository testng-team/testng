package test.factory;

import org.testng.annotations.Test;

public class BaseFactorySample {

  private final int n;

  public BaseFactorySample(int n) {
    this.n = n;
  }

  public int getN() {
    return n;
  }

  @Test
  public void f() {}

  /**
   * @@@ for some reason, the test results get added in the wrong order if I don't define a
   * toString() method. Need to investigate. https://github.com/cbeust/testng/issues/799 TODO Remove
   * the method when issue will be fixed
   */
  @Override
  public String toString() {
    return "[" + getClass().getName() + " " + getN() + "]";
  }
}
