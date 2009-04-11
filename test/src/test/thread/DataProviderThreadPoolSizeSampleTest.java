package test.thread;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class DataProviderThreadPoolSizeSampleTest {
  static Map<Long, Long> m_threadIds;
  
  @BeforeClass(alwaysRun = true)
  public void setUp() {
    ppp("INIT THREAD IDS");
    m_threadIds = new HashMap<Long, Long>();    
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
    };
  }

  @Test(dataProvider = "sequentialDataProvider", groups = "sequential")
  public void fSequential(Integer p) {
    long n = Thread.currentThread().getId();
    ppp("fSequential THREAD: " + n + " " +hashCode());
    m_threadIds.put(n,n);
  }

  @Test(dataProvider = "parallelDataProvider", groups = "parallel")
  public void fParallel(Integer p) {
    long n = Thread.currentThread().getId();
    ppp("fParallel THREAD: " + n + " " +hashCode());
    m_threadIds.put(n,n);
  }
  
//  @Test(dependsOnMethods = {"f1"})
//  public void verify() {
//    int expected = 3;
//    Assert.assertEquals(m_threadIds.size(), expected,  
//        "Should have run on " + expected + " threads but ran on " + m_threadIds.size());
//  }
  
  private static void ppp(String s) {
    if (false) {
      System.out.println("[ThreadPoolSizeTest] " + s);
    }
  }
}
