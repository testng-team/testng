package test.inject.parameterresolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResolvedAfterDataProviderSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"value"}};
  }

  @Test(dataProvider = "dp")
  public void test(String fromDataProvider, @FromResolver CustomObject custom) {
    ParameterRecorder.record("test", fromDataProvider, custom);
  }
}
