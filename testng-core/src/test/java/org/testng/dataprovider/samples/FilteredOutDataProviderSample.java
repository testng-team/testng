package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Non-empty data provider whose every row is excluded by {@code indices}. */
public class FilteredOutDataProviderSample {

  @DataProvider(indices = 5)
  public Object[][] dp() {
    return new Object[][] {{"a"}, {"b"}};
  }

  @Test(dataProvider = "dp", invocationCount = 3)
  public void sequential(String value) {
    fail();
  }

  @Test(dataProvider = "dp", invocationCount = 3, threadPoolSize = 2)
  public void pooled(String value) {
    fail();
  }
}
