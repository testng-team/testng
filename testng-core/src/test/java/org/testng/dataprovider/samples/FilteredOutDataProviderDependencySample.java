package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Non-empty data provider, every row excluded by {@code indices}, on a method a failing dependency
 * already skips.
 */
public class FilteredOutDataProviderDependencySample {

  @Test
  public void failing() {
    throw new IllegalStateException("this test fails on purpose");
  }

  @DataProvider(indices = 5)
  public Object[][] dp() {
    return new Object[][] {{"a"}, {"b"}};
  }

  @Test(dataProvider = "dp", dependsOnMethods = "failing")
  public void skipped(String value) {
    fail();
  }
}
