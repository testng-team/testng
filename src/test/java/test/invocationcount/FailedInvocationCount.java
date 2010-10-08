package test.invocationcount;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FailedInvocationCount {
  int m_count;

  @BeforeClass
  public void setUp() {
    m_count = 0;
  }

  @Test(invocationCount = 10)
  public void f() {
    if (m_count++ > 3) {
      throw new RuntimeException();
    }
  }

}
