package test.thread;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderThreadPoolSizeSampleTest extends BaseThreadTest {
  @BeforeClass(alwaysRun = true)
  public void setUp() {
    log(getClass().getName(), "Init log ids");
    initThreadLog();
  }

  @DataProvider(parallel = true)
  public Object[][] parallelDataProvider() {
    return createArray();
  }

  @DataProvider
  public Object[][] sequentialDataProvider() {
    return createArray();
  }

  private Object[][] createArray() {
    int i = 0;
    return new Object[][] {
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
        new Object[] { i++ },
    };
  }

  @Test(dataProvider = "sequentialDataProvider", groups = "sequential")
  public void fSequential(Integer p) {
    long n = Thread.currentThread().getId();
    log(getClass().getName(), "Sequential");
    logThread(n);
  }

  @Test(dataProvider = "parallelDataProvider", groups = "parallel")
  public void fParallel(Integer p) {
    long n = Thread.currentThread().getId();
    log(getClass().getName(), "Parallel");
    logThread(n);
  }

//  @Test(dependsOnMethods = {"f1"})
//  public void verify() {
//    int expected = 3;
//    Assert.assertEquals(m_threadIds.size(), expected,
//        "Should have run on " + expected + " threads but ran on " + m_threadIds.size());
//  }
}
