package test.dataprovider.issue3263;

import org.testng.annotations.DataProvider;

public class DataProviderBeta {
  @DataProvider(name = "places")
  public Object[][] places() {
    return new Object[][] {{"beta_place1", "beta_city1"}, {"beta_place2", "beta_city2"}};
  }
}
