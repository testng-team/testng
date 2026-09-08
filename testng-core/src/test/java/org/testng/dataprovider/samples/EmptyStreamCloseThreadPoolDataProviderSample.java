package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmptyStreamCloseThreadPoolDataProviderSample {

  public static final AtomicInteger OPEN_COUNT = new AtomicInteger(0);
  public static final AtomicInteger CLOSE_COUNT = new AtomicInteger(0);

  @DataProvider
  public Stream<Object[]> dp() {
    OPEN_COUNT.incrementAndGet();
    return Stream.<Object[]>empty().onClose(CLOSE_COUNT::incrementAndGet);
  }

  @Test(dataProvider = "dp", invocationCount = 3, threadPoolSize = 2)
  public void test() {
    fail();
  }
}
