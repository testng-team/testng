package test.factory;

import java.util.ArrayList;
import java.util.List;

public class FactoryWithDataProvider {
  
  /**
   * @testng.data-provider
   */
  public Object[][] dp() {
    return new Object[][] {
      new Object[] { new int[] { 3, 5 } },
      new Object[] { new int [] { 7, 9 } },
    };
  }

  /**
   * @testng.factory dataProvider = "dp"
   */
  public Object[] factory(int[] array) {
    List result = new ArrayList();
    for (int i = 0; i < array.length; i++) {
      result.add(new OddTest(array[i]));
    }
    
    return result.toArray();
  }
  
  private static void ppp(String s) {
    System.out.println("[FactoryWithDataProvider] " + s);
  }

}
