package test.inject.parameterresolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * No resolver claims the {@link CustomObject} here -- it carries no {@link FromResolver} -- so the
 * data provider still has to account for it, and does not.
 */
public class UnsupportedParameterSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"value"}};
  }

  @Test(dataProvider = "dp")
  public void test(CustomObject notResolved, String fromDataProvider) {
    ParameterRecorder.record("test", notResolved, fromDataProvider);
  }
}
