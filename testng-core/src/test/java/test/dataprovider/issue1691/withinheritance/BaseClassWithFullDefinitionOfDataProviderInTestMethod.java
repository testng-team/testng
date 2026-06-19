package test.dataprovider.issue1691.withinheritance;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;
import test.dataprovider.issue1691.SampleDataProvider;

public class BaseClassWithFullDefinitionOfDataProviderInTestMethod {

  @Test(dataProviderClass = SampleDataProvider.class, dataProvider = "hangoutPlaces")
  public void verifyHangoutPlaces(String place, String city) {
    assertThat(place).isNotNull();
    assertThat(city).isNotNull();
  }
}
