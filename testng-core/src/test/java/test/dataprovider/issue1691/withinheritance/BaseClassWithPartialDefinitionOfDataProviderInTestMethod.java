package test.dataprovider.issue1691.withinheritance;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class BaseClassWithPartialDefinitionOfDataProviderInTestMethod {

  @Test(dataProvider = "hangoutPlaces")
  public void verifyHangoutPlaces(String place, String city) {
    assertThat(place).isNotNull();
    assertThat(city).isNotNull();
  }
}
