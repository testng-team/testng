package test.dataprovider.issue1987;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(DataProviderTrackingListener.class)
public class DataProviderInSameClass {

  @Test(dataProvider = "dp")
  public void testMethod(int i) {
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][]{{1}, {2}};
  }
}
