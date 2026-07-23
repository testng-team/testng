package test.dataprovider.issue1691;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

@Test(dataProviderClass = SampleDataProvider.class, dataProvider = "busyMalls")
public class DataProviderDefinitionCompletelyProvidedAtClassLevelAndPartiallyAtMethodLevel {

  @Test(dataProvider = "hangoutPlaces")
  public void verifyHangoutPlaces(String place, String city) {
    assertThat(place).isNotNull();
    assertThat(city).isNotNull();
  }
}
