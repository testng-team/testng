package test.dataprovider.issue1691.withinheritance;

import org.testng.Assert;
import org.testng.annotations.Test;
import test.dataprovider.issue1691.SampleDataProvider;

public class BaseClassWithFullDefinitionOfDataProviderInTestMethod {

  @Test(dataProviderClass = SampleDataProvider.class, dataProvider = "hangoutPlaces")
  public void verifyHangoutPlaces(String place, String city) {
    Assert.assertNotNull(place);
    Assert.assertNotNull(city);
  }
}
