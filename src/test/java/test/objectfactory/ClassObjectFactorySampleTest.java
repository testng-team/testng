package test.objectfactory;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ClassObjectFactorySampleTest {
  public static int m_n = 0;

  /**
   * Will be invoked with 42 by the factory.
   */
  public ClassObjectFactorySampleTest(int n) {
    m_n = n;
  }

  @Test
  public void f() {
    Assert.assertEquals(42, m_n);
  }
}
