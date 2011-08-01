package test.factory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryDataProviderSampleTest extends BaseFactory {

  @Factory(dataProvider = "dp")
  public FactoryDataProviderSampleTest(int n) {
    super(n);
  }

  @DataProvider
  static public Object[][] dp() {
    return new Object[][] {
      new Object[] { 41 },
      new Object[] { 42 },
    };
  }

  @Override
  public String toString() {
    return "[FactoryDataProviderSampleTest " + getN() + "]";
  }

  @Test
  public void f() {
//    System.out.println("Test:" + getN());
  }
}
