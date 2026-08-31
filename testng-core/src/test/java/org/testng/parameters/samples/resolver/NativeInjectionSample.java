package test.inject.parameterresolver;

import java.lang.reflect.Method;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** The three sources at once: native injection, a resolver, and a data provider. */
public class NativeInjectionSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"value"}};
  }

  @Test(dataProvider = "dp")
  public void test(
      Method currentMethod,
      @FromResolver CustomObject custom,
      String fromDataProvider,
      ITestContext context) {
    ParameterRecorder.record("test", currentMethod, custom, fromDataProvider, context);
  }
}
