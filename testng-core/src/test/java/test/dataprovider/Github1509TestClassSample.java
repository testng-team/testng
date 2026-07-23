package test.dataprovider;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Github1509TestClassSample {
  @Test(dataProvider = "dp")
  public void demo(int i) {
    assertThat(i > 0).isTrue();
  }

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return null;
  }
}
