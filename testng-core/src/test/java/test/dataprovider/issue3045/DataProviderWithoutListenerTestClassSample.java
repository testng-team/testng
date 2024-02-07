package test.dataprovider.issue3045;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderWithoutListenerTestClassSample {

  @DataProvider
  public Object[][] dataProvider() {
    return new Object[][] {{"Test_1"}, {"Test_2"}, {"Test_3"}};
  }

  @Test(dataProvider = "dataProvider")
  public void dataDrivenTest(String ignored) {}

  @Test
  public void normalTest() {}
}
