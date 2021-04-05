package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.annotations.TestInstance;

import java.lang.reflect.Method;

public class TestInstanceSample {

  public static int m_instanceCount = 0;

  private final int m_n;

  public TestInstanceSample() {
    this(0);
  }

  public TestInstanceSample(int n) {
    this.m_n = n;
  }

  @DataProvider
  public Object[][] dp(Method m, @TestInstance Object instance) {
    TestInstanceSample o0 = (TestInstanceSample) instance;
    Assert.assertTrue(o0.m_n == 1 || o0.m_n == 2);
    m_instanceCount++;

    return new Object[][] {{42}, {43}};
  }

  @Test(dataProvider = "dp")
  public void f(int o) {}

  @Override
  public String toString() {
    return "[A n:" + m_n + "]";
  }
}
