package org.testng.dataprovider.samples;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** Data provider that hands out no row on its third call and rows again afterwards. */
public class PartiallyEmptyDataProviderSample {

  public static final AtomicInteger CALLS = new AtomicInteger(0);
  public static final AtomicInteger BODIES = new AtomicInteger(0);

  @DataProvider
  public Object[][] dp() {
    return CALLS.incrementAndGet() == 3 ? new Object[0][] : new Object[][] {{"a"}};
  }

  @Test(dataProvider = "dp", invocationCount = 5)
  public void test(String value) {
    BODIES.incrementAndGet();
  }
}
