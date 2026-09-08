package org.testng.dataprovider.samples;

import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class EmptyStreamCloseDataProviderSample {

  public static final AtomicInteger CLOSE_COUNT = new AtomicInteger(0);

  @DataProvider
  public Stream<Object[]> dp() {
    return Stream.<Object[]>empty().onClose(CLOSE_COUNT::incrementAndGet);
  }

  @Test(dataProvider = "dp")
  public void test() {
    fail();
  }
}
