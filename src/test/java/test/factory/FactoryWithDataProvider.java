package test.factory;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

import java.util.ArrayList;
import java.util.List;

public class FactoryWithDataProvider {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] { new int[] { 3, 5 } },
      new Object[] { new int [] { 7, 9 } },
    };
  }

  @Factory(dataProvider = "dp")
  public Object[] factory(int[] array) {
    List<Object> result = new ArrayList<>();
    for (int n : array) {
      result.add(new OddTest(n));
    }

    return result.toArray();
  }

  private static void ppp(String s) {
    System.out.println("[FactoryWithDataProvider] " + s);
  }

}
