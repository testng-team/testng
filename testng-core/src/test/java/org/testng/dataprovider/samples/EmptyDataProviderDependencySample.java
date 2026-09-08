package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmptyDataProviderDependencySample {

  @DataProvider
  public Object[][] emptyDp() {
    return new Object[0][];
  }

  @Test(dataProvider = "emptyDp")
  public void producer(String value) {
    fail();
  }

  @Test(dependsOnMethods = "producer")
  public void consumer() {}
}
