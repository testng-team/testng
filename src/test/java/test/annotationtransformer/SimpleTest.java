package test.annotationtransformer;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleTest {
  private int m_n;

  public SimpleTest(int n) {
    m_n = n;
  }

  @Test
  public void run() {
    Assert.assertEquals(m_n, 42);
  }
}
