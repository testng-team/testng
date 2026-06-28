package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.annotations.TestInstance;

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
    assertThat(o0.m_n == 1 || o0.m_n == 2).isTrue();
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
