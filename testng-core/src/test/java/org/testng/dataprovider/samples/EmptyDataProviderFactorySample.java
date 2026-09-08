package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class EmptyDataProviderFactorySample {

  @Factory
  public static Object[] instances() {
    return new Object[] {
      new EmptyDataProviderFactorySample(), new EmptyDataProviderFactorySample()
    };
  }

  @DataProvider
  public Object[][] emptyDp() {
    return new Object[0][];
  }

  @Test(dataProvider = "emptyDp")
  public void test(String value) {
    fail();
  }
}
