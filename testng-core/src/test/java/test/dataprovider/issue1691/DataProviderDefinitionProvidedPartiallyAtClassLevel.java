package test.dataprovider.issue1691;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(dataProviderClass = SampleDataProvider.class, dataProvider = "hangoutPlaces")
public class DataProviderDefinitionProvidedPartiallyAtClassLevel {

  @Test
  public void verifyHangoutPlaces(String place, String city) {
    Assert.assertNotNull(place);
    Assert.assertNotNull(city);
  }
}
