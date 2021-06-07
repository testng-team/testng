package test.dataprovider.issue1691.withinheritance;

import org.testng.Assert;

public class ChildClassWithNoDataProviderInformationInTestMethod
    extends BaseClassWithFullDefinitionOfDataProviderInClassLevel {

  public void verifyHangoutPlaces(String place, String city) {
    Assert.assertNotNull(place);
    Assert.assertNotNull(city);
  }
}
