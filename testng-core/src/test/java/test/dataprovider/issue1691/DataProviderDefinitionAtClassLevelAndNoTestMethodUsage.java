package test.dataprovider.issue1691;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(dataProviderClass = SampleDataProvider.class, dataProvider = "hangoutPlaces")
public class DataProviderDefinitionAtClassLevelAndNoTestMethodUsage {

  public void regularTestMethod() {}

  public void verifyHangoutPlaces(String place, String city) {
    Assert.assertNotNull(place);
    Assert.assertNotNull(city);
  }
}
