package test.issue565.deadlock;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class GeneratedClassFactory {

  public static final int SIZE = 20;

  @DataProvider(name = "ids", parallel = true)
  public Object[][] ids() {
    Integer[][] params = new Integer[SIZE][1];
    for (int id = 0; id < params.length; id++) {
      params[id] = new Integer[] {id};
    }
    return params;
  }

  @Factory(dataProvider = "ids")
  public Object[] generate(int id) {
    return new Object[] {new GeneratedClassInGroupA(id)};
  }
}
