package org.testng.factory.samples;

import org.testng.annotations.DataProvider;

public class NotStaticDataProvider {

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {
      new Object[] {43}, new Object[] {44},
    };
  }
}
