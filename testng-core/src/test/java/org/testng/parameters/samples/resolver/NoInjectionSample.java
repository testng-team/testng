package test.inject.parameterresolver;

import java.lang.reflect.Method;
import org.testng.annotations.DataProvider;
import org.testng.annotations.NoInjection;
import org.testng.annotations.Test;

/**
 * {@code @NoInjection} still hands the parameter to the data provider when no resolver wants it.
 */
public class NoInjectionSample {

  @DataProvider(name = "dp")
  public Object[][] dp() throws Exception {
    return new Object[][] {{NoInjectionSample.class.getMethod("aMethodFromTheDataProvider")}};
  }

  public void aMethodFromTheDataProvider() {}

  @Test(dataProvider = "dp")
  public void test(@NoInjection Method fromDataProvider) {
    ParameterRecorder.record("test", fromDataProvider);
  }
}
