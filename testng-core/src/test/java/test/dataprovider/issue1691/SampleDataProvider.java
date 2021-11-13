package test.dataprovider.issue1691;

import org.testng.annotations.DataProvider;

public class SampleDataProvider {

  @DataProvider
  public Object[][] hangoutPlaces() {
    return new Object[][] {
      {"Hakuna Matata", "Bangalore"},
      {"Gem Inn", "Chennai"}
    };
  }

  @DataProvider
  public Object[][] busyMalls() {
    return new Object[][] {
      {"Orion Mall", "Bangalore"},
      {"Phoenix Mall", "Chennai"}
    };
  }
}
