package org.testng.factory.samples.issue3576;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class PooledFactorySample {

  private final String name;

  @Factory(dataProvider = "names")
  public PooledFactorySample(String name) {
    this.name = name;
  }

  @DataProvider
  public static Object[][] names() {
    return new Object[][] {{"alpha"}, {"beta"}};
  }

  @Test(invocationCount = 2)
  public void serial() {}

  @Test(invocationCount = 2, threadPoolSize = 2)
  public void pooled() {}

  @Override
  public String toString() {
    return name;
  }
}
