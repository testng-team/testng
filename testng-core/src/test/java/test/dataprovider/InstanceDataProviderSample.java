package test.dataprovider;

import static java.lang.Integer.valueOf;
import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InstanceDataProviderSample {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {{hashCode()}};
  }

  @Test(dataProvider = "dp")
  public void f(Integer n) {
    assertThat(n).isEqualTo(valueOf(hashCode()));
  }
}
