package test.inject.parameterresolver;

import java.lang.reflect.Method;
import org.mockito.Mock;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MockitoSample {

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {{"Ada"}, {"Grace"}};
  }

  @Test(dataProvider = "dp")
  public void test(
      Method currentMethod, @Mock Greeter greeter, String name, @Mock Counter counter) {
    ParameterRecorder.record("test", currentMethod, greeter, name, counter);
  }
}
