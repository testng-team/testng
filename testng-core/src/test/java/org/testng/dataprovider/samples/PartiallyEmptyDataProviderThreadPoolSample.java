package org.testng.dataprovider.samples;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Data provider backed by a finite supply: it runs out while the pool is still invoking. */
public class PartiallyEmptyDataProviderThreadPoolSample {

  public static final AtomicInteger CALLS = new AtomicInteger(0);

  @DataProvider
  public Object[][] dp() {
    return CALLS.incrementAndGet() <= 2 ? new Object[][] {{"a"}} : new Object[0][];
  }

  @Test(dataProvider = "dp", invocationCount = 3, threadPoolSize = 2)
  public void test(String value) {}
}
