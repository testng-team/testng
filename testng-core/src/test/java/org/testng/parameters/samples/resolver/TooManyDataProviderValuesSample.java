package test.inject.parameterresolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** One resolved parameter, one the provider must supply -- and a row that supplies two. */
public class TooManyDataProviderValuesSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"value", "surplus"}};
  }

  @Test(dataProvider = "dp")
  public void test(@FromResolver CustomObject custom, String fromDataProvider) {
    ParameterRecorder.record("test", custom, fromDataProvider);
  }
}
