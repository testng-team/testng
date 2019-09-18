package test.dataprovider.typed;

import org.testng.annotations.DataProvider;
import org.testng.annotations.ParameterCollector;
import org.testng.annotations.Test;

public class TypedDataProviderSample {

  private boolean dpDone = false;

  @Test(dataProvider = "dp")
  public void doStuff(String a, int b) {
    if (!dpDone) throw new RuntimeException("Method should not be actually called by data provider");
  }

  @DataProvider(name = "dp")
  public void createData(@ParameterCollector TypedDataProviderSample test) {
    test.doStuff("test1", 1);
    test.doStuff("test2", 2);
    dpDone = true;
  }

  @Test(dataProvider = "dp2")
  public void doStuff2(String a, int b) {}

  @DataProvider(name = "dp2")
  public void createData2(@ParameterCollector TypedDataProviderSample test) {
    test.doStuff("test1", 1); // calling wrong method, should be doStuff2
  }
}
