package test.dataprovider.issue1691.withinheritance;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BaseClassWithPartialDefinitionOfDataProviderInTestMethod {

  @Test(dataProvider = "hangoutPlaces")
  public void verifyHangoutPlaces(String place, String city) {
    Assert.assertNotNull(place);
    Assert.assertNotNull(city);
  }
}
