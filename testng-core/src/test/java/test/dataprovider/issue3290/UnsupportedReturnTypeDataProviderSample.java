package test.dataprovider.issue3290;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * A data provider whose return type is none of the supported ones. Used to confirm the guidance in
 * the resulting error message now lists {@code Stream} amongst the accepted return types.
 */
public class UnsupportedReturnTypeDataProviderSample {

  @DataProvider(name = "dp")
  public String dp() {
    return "this is not a supported data provider return type";
  }

  @Test(dataProvider = "dp")
  public void testMethod(String value) {}
}
