package test.github1490;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryPoweredDataProviderWithoutListenerAnnotationSample {
  private final int i;

  @Factory(dataProvider = "dp")
  public FactoryPoweredDataProviderWithoutListenerAnnotationSample(int i) {
    this.i = i;
  }

  @Test
  public void testMethod() {
    assertThat(i > 0).isTrue();
  }

  @DataProvider(name = "dp")
  public static Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
