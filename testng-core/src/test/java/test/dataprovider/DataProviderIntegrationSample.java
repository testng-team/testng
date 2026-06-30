package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderIntegrationSample {
  @DataProvider
  public Object[][] testInts() {
    return new Object[][] {new Object[] {4}, new Object[] {8}, new Object[] {12}};
  }

  @Test(dataProvider = "testInts", expectedExceptions = IllegalArgumentException.class)
  public void theTest(String aString) {
    assertThat(aString).isNotNull();
  }
}
