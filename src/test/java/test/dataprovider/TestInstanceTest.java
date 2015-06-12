package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.annotations.TestInstance;

import java.lang.reflect.Method;

public class TestInstanceTest {

  private int m_n;
  private static int m_instanceCount = 0;

  public TestInstanceTest() {}

  public TestInstanceTest(int n) {
    this.m_n = n;
  }

  @DataProvider
  public Object[][] dp(Method m, @TestInstance Object instance) {
    TestInstanceTest o0 = (TestInstanceTest) instance;
    Assert.assertTrue(o0.m_n == 1 || o0.m_n == 2);
    m_instanceCount++;
    return new Object[][] {
        new Object[] {42},
        new Object[] {43},
    };
  }

  @Test(dataProvider = "dp")
  public void f(int o) {
  }

  @Override
  public String toString() {
    return "[A n:" + m_n + "]";
  }

  private static void ppp(String s) {
    System.out.println("[A] " + s);
  }
}