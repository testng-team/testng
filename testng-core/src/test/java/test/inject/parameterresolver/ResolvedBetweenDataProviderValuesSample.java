package test.inject.parameterresolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResolvedBetweenDataProviderValuesSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"value", 42}};
  }

  @Test(dataProvider = "dp")
  public void test(String a, @FromResolver CustomObject custom, int b) {
    ParameterRecorder.record("test", a, custom, b);
  }
}
