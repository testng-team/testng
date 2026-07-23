package test.github1490;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SimpleDataProviderWithoutListenerAnnotationSample {
  @Test(dataProvider = "getData")
  public void testMethod(int i) {
    assertThat(i > 0).isTrue();
  }

  @DataProvider
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
