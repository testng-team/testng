package test.factory;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class FactoryWithDataProvider {
  
  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] { 3 },
      new Object[] { 7 },
    };
  }
  
  @Factory(dataProvider = "dp")
  public Object[] factory(int array) {
    ppp("FACTORY " + array);
    List<Object> result = new ArrayList<Object>();
//    for (int n : array) {
//      result.add(new OddTest(n));
//    }
    
    return result.toArray();
  }
  
  private static void ppp(String s) {
    System.out.println("[FactoryWithDataProvider] " + s);
  }

}
