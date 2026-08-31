package test.inject.parameterresolver;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * The trailing array is matched by {@code ArrayEndingMethodMatcher}, which has to work off the
 * parameter set the resolver was already taken out of.
 */
public class VarargsSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"one", "two", "three"}};
  }

  @Test(dataProvider = "dp")
  public void test(@FromResolver CustomObject custom, String... rest) {
    ParameterRecorder.record("test", custom, rest);
  }
}
