package test.dataprovider.issue3565;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** GITHUB-3565. A test whose data provider throws. */
public class BrokenDataProviderSample {

  @DataProvider
  public Object[][] rows() {
    throw new IllegalStateException("The data provider fails");
  }

  @Test(dataProvider = "rows")
  public void test(String value) {}
}
