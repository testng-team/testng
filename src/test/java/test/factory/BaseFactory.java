package test.factory;

import org.testng.annotations.Test;

public class BaseFactory {

  private int m_n;

  public BaseFactory(int n) {
    m_n = n;
  }

  public int getN() {
    return m_n;
  }

  @Test
  public void f() {
  }

  /**
   * @@@ for some reason, the test results get added in the wrong order if
   * I don't define a toString() method. Need to investigate.
   */
  @Override
  public String toString() {
    return "[" + getClass().getName() + " " + getN() + "]";
  }
}
