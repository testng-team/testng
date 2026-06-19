package test.thread;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class DataProviderThreadPoolSizeTest extends SimpleBaseTest {

  @Test
  public void shouldUseDefaultDataProviderThreadCount() {
    TestNG tng = create(DataProviderThreadPoolSizeSampleTest.class);
    tng.setGroups("parallel");
    tng.run();
    assertThat(DataProviderThreadPoolSizeSampleTest.getThreadCount()).isEqualTo(10);
  }

  @Test
  public void shouldNotUseThreadsIfNotUsingParallel() {
    TestNG tng = create(DataProviderThreadPoolSizeSampleTest.class);
    tng.setGroups("sequential");
    tng.run();
    assertThat(DataProviderThreadPoolSizeSampleTest.getThreadCount()).isEqualTo(1);
  }

  @Test
  public void shouldUseSpecifiedDataProviderThreadCount() {
    TestNG tng = create(DataProviderThreadPoolSizeSampleTest.class);
    tng.setGroups("parallel");
    tng.setDataProviderThreadCount(3);
    tng.run();
    assertThat(DataProviderThreadPoolSizeSampleTest.getThreadCount()).isEqualTo(3);
  }
}
