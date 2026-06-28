package test.reports;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GitHub1148Sample {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {{"Cedric"}, {"Anne"}};
  }

  @Test(dataProvider = "dp")
  public void verifyData(String n1) {
    assertThat(n1).isEqualTo("Cedric");
  }
}
