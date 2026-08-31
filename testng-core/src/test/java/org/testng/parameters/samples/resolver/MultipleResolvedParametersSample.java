package test.inject.parameterresolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MultipleResolvedParametersSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"value"}};
  }

  @Test(dataProvider = "dp")
  public void test(
      @FromResolver CustomObject first,
      String fromDataProvider,
      @FromResolver CustomObject second) {
    ParameterRecorder.record("test", first, fromDataProvider, second);
  }
}
