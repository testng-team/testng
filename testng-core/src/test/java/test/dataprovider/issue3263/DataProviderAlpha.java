package test.dataprovider.issue3263;

import org.testng.annotations.DataProvider;

public class DataProviderAlpha {
  @DataProvider(name = "places")
  public Object[][] places() {
    return new Object[][] {{"alpha_place1", "alpha_city1"}, {"alpha_place2", "alpha_city2"}};
  }
}
