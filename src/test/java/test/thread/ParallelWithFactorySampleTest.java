package test.thread;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class ParallelWithFactorySampleTest extends BaseSequentialSample {
  private int m_n;

  @DataProvider
  public static Object[][] dp() {
    return new Object[][] {
        new Object[] { 42 },
        new Object[] { 43 }
    };
  }

  @Factory(dataProvider = "dp")
  public ParallelWithFactorySampleTest(int n) {
    m_n = n;
  }


  protected int getN() {
    return m_n;
  }


  @Test
  public void f1() {
    addId("f1 " + getN(), Thread.currentThread().getId());
  }

  @Test
  public void f2() {
    addId("f2", Thread.currentThread().getId());
  }
}
