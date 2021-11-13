package test.multiple;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

public class Test1 {
  private static int m_count = 0;

  @Test
  public void f1() {
    assertTrue(m_count < 1, "FAILING");
    m_count++;
  }

  @AfterTest
  public void cleanUp() {
    m_count = 0;
  }
}
