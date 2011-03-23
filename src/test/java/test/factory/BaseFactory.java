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
}
