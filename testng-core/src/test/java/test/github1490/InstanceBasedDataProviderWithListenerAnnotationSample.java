package test.github1490;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import test.listeners.github1490.DataProviderInfoProvider;

@Listeners(DataProviderInfoProvider.class)
public class InstanceBasedDataProviderWithListenerAnnotationSample {
  @Test(dataProvider = "getData")
  public void testMethod(int i) {
    assertThat(i).isPositive();
  }

  @DataProvider
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
