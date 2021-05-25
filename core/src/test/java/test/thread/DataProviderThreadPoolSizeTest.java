package test.thread;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;

public class DataProviderThreadPoolSizeTest extends SimpleBaseTest {

  @Test
  public void shouldUseDefaultDataProviderThreadCount() {
    TestNG tng = create(DataProviderThreadPoolSizeSampleTest.class);
    tng.setGroups("parallel");
    tng.run();
    Assert.assertEquals(DataProviderThreadPoolSizeSampleTest.getThreadCount(), 10);
  }

  @Test
  public void shouldNotUseThreadsIfNotUsingParallel() {
    TestNG tng = create(DataProviderThreadPoolSizeSampleTest.class);
    tng.setGroups("sequential");
    tng.run();
    Assert.assertEquals(DataProviderThreadPoolSizeSampleTest.getThreadCount(), 1);
  }

  @Test
  public void shouldUseSpecifiedDataProviderThreadCount() {
    TestNG tng = create(DataProviderThreadPoolSizeSampleTest.class);
    tng.setGroups("parallel");
    tng.setDataProviderThreadCount(3);
    tng.run();
    Assert.assertEquals(DataProviderThreadPoolSizeSampleTest.getThreadCount(), 3);
  }
}
