package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

/**
 * Pooled counterpart of {@link EmptyDataProviderFactorySample}: the invocations of each factory
 * instance run in their own thread pool, so the method is cloned before the skip is reported.
 */
public class EmptyDataProviderFactoryThreadPoolSample {

  private final String name;

  public EmptyDataProviderFactoryThreadPoolSample(String name) {
    this.name = name;
  }

  @Factory
  public static Object[] instances() {
    return new Object[] {
      new EmptyDataProviderFactoryThreadPoolSample("first"),
      new EmptyDataProviderFactoryThreadPoolSample("second")
    };
  }

  public String name() {
    return name;
  }

  @DataProvider
  public Object[][] emptyDp() {
    return new Object[0][];
  }

  @Test(dataProvider = "emptyDp", invocationCount = 3, threadPoolSize = 2)
  public void test(String value) {
    fail();
  }
}
