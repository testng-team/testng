package test.factory;

import org.testng.Assert;
import org.testng.annotations.Test;

public class OddTest {
  private int m_n;
  
  public OddTest(int n) {
    m_n = n;
  }
  
  @Test
  public void verify() {
    Assert.assertTrue(m_n % 2 == 1);
  }
}
