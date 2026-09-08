package org.testng.dataprovider.samples;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Sequential counterpart of {@link DataProviderInvocationCountThreadPoolSample}. */
public class DataProviderInvocationCountSample {

  public static final AtomicInteger DATA_PROVIDER_INVOCATIONS = new AtomicInteger(0);

  @DataProvider
  public Object[][] dp() {
    DATA_PROVIDER_INVOCATIONS.incrementAndGet();
    return new Object[][] {{"a"}, {"b"}};
  }

  @Test(dataProvider = "dp", invocationCount = 3)
  public void test(String value) {}
}
