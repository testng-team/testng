package test.retryAnalyzer;

import org.testng.annotations.Factory;

public class MyFactory {

  private static final int NUM = 10;

  @Factory
  public Object[] createTests() {
    Object[] result = new Object[NUM];
    for (int i = 0; i < NUM; i++) {
      result[i] = new FactoryTest(i);
    }
    return result;
  }
}
