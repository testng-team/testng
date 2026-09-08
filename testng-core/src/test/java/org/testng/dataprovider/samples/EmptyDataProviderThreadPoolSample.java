package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmptyDataProviderThreadPoolSample {

  public static final AtomicInteger DATA_PROVIDER_INVOCATIONS = new AtomicInteger(0);

  @DataProvider
  public Object[][] emptyDp() {
    DATA_PROVIDER_INVOCATIONS.incrementAndGet();
    return new Object[0][];
  }

  @Test(dataProvider = "emptyDp", invocationCount = 3, threadPoolSize = 2)
  public void testPooledInvocations() {
    fail();
  }
}
