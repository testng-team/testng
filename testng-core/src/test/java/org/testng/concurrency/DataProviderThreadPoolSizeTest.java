package org.testng.concurrency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.concurrency.samples.BaseThreadTest.getThreadCount;

import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.concurrency.samples.DataProviderThreadPoolSizeSampleTest;
import test.SimpleBaseTest;

public class DataProviderThreadPoolSizeTest extends SimpleBaseTest {

  @Test
  public void shouldUseDefaultDataProviderThreadCount() {
    TestNG tng = create(DataProviderThreadPoolSizeSampleTest.class);
    tng.setGroups("parallel");
    tng.run();
    assertThat(getThreadCount()).isEqualTo(10);
  }

  @Test
  public void shouldNotUseThreadsIfNotUsingParallel() {
    TestNG tng = create(DataProviderThreadPoolSizeSampleTest.class);
    tng.setGroups("sequential");
    tng.run();
    assertThat(getThreadCount()).isEqualTo(1);
  }

  @Test
  public void shouldUseSpecifiedDataProviderThreadCount() {
    TestNG tng = create(DataProviderThreadPoolSizeSampleTest.class);
    tng.setGroups("parallel");
    tng.setDataProviderThreadCount(3);
    tng.run();
    assertThat(getThreadCount()).isEqualTo(3);
  }
}
