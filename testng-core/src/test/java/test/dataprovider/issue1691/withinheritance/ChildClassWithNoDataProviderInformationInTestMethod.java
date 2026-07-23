package test.dataprovider.issue1691.withinheritance;

import static org.assertj.core.api.Assertions.assertThat;

public class ChildClassWithNoDataProviderInformationInTestMethod
    extends BaseClassWithFullDefinitionOfDataProviderInClassLevel {

  public void verifyHangoutPlaces(String place, String city) {
    assertThat(place).isNotNull();
    assertThat(city).isNotNull();
  }
}
