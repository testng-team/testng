package org.testng.dataprovider.samples;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Same data provider and invocationCount as {@link DataProviderInvocationCountSample}, pooled. */
public class DataProviderInvocationCountThreadPoolSample {

  public static final AtomicInteger DATA_PROVIDER_INVOCATIONS = new AtomicInteger(0);

  @DataProvider
  public Object[][] dp() {
    DATA_PROVIDER_INVOCATIONS.incrementAndGet();
    return new Object[][] {{"a"}, {"b"}};
  }

  @Test(dataProvider = "dp", invocationCount = 3, threadPoolSize = 2)
  public void test(String value) {}
}
