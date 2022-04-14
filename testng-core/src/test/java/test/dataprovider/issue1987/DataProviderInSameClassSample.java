package test.dataprovider.issue1987;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderInSameClassSample {

  @Test(dataProvider = "dp")
  public void testMethod(int i) {}

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
