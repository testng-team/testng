package test.dataprovider.issue1987;

import org.testng.annotations.DataProvider;

public class BaseClassSample {

  @DataProvider(name = "dp")
  public Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }
}
