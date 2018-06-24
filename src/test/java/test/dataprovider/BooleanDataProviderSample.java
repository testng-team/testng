package test.dataprovider;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BooleanDataProviderSample {

  @Test(dataProvider = "allBooleans")
  public void doStuff(boolean t) {}

  @DataProvider(name = "allBooleans")
  public Object[][] createData() {
    return new Object[][] {new Object[] {true}, new Object[] {false}};
  }
}
