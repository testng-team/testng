package test.annotationtransformer;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListenerAdapter.class)
public class SimpleTest {
  private int m_n;

  public SimpleTest(int n) {
    m_n = n;
  }

  @BeforeMethod
  public void beforeMethod() {}

  @Test
  public void run() {
    Assert.assertEquals(m_n, 42);
  }
}
