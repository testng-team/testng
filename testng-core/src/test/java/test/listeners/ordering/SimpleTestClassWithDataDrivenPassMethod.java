package test.listeners.ordering;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SimpleTestClassWithDataDrivenPassMethod {

  @Test(dataProvider = "dp")
  public void testWillPass(int i) {}

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {
        {1},
        {2}
    };
  }

}
