package test.retryAnalyzer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * retryAnalyzer parameter unit tests.
 *
 * @author tocman@gmail.com (Jeremie Lenfant-Engelmann)
 */
public final class InvocationCountTest implements IRetryAnalyzer {
  static final Multiset<String> invocations = ConcurrentHashMultiset.create();
  private static final AtomicInteger retriesRemaining = new AtomicInteger(100);
  private static final int MAX_RETRY = 2;

  static int tcid1 = 0;
  static int tcid2 = 0;
  static int tcid3 = 0;

  private int r1 = 0;
  private int r2 = 1;
  private int r3 = 0;
  private int r7 = 0;
  private static int value = 42;
  private int executionNumber = 0;

  @Test(retryAnalyzer = InvocationCountTest.class)
  public void testAnnotationWithNoRetries() {}

  @Test(retryAnalyzer = InvocationCountTest.class)
  public void testAnnotationWithOneRetry() {
    if (r1++ < 1) {
      fail();
    }
  }

  @Test(retryAnalyzer = InvocationCountTest.class)
  public void testAnnotationWithSevenRetries() {
    if (r7++ < 7) {
      fail();
    }
  }

  @Test(retryAnalyzer = ThreeRetries.class, successPercentage = 0)
  public void failAfterThreeRetries() {
    fail();
  }

  @Test(
      dependsOnMethods = {
        "testAnnotationWithNoRetries",
        "testAnnotationWithOneRetry",
        "testAnnotationWithSevenRetries",
        "failAfterThreeRetries"
      },
      alwaysRun = true)
  public void checkInvocationCounts() {
    assertEquals(invocations.count("testAnnotationWithNoRetries"), 0);
    assertEquals(invocations.count("testAnnotationWithOneRetry"), 1);
    assertEquals(invocations.count("testAnnotationWithSevenRetries"), 7);
    assertEquals(invocations.count("failAfterThreeRetries"), 4);
  }

  @Test(retryAnalyzer = InvocationCountTest.class, dataProvider = "dataProvider3")
  public void retryWithDataProvider(String tc) {
    if ("tc1".equals(tc)) {

      if (tcid1++ < MAX_RETRY) {
        fail();
      }
    }
    if ("tc2".equals(tc)) {
      if (tcid2++ < MAX_RETRY) {
        fail();
      }
    }
    if ("tc3".equals(tc)) {
      if (tcid3++ < MAX_RETRY) {
        fail();
      }
    }
  }

  @Test(
      dependsOnMethods = {"retryWithDataProvider"},
      alwaysRun = true)
  public void checkRetryCounts() {
    assertEquals(tcid1, 3);
    assertEquals(tcid2, 3);
    assertEquals(tcid3, 3);
  }

  @DataProvider(name = "dataProvider")
  private Object[][] dataProvider() {
    return new Object[][] {{1, true}, {2, false}, {3, true}, {4, false}};
  }

  @DataProvider(name = "dataProvider2")
  private Object[][] dataProvider2() {
    value = 42;

    return new Object[][] {{true}, {true}};
  }

  @DataProvider(name = "dataProvider3")
  private Object[][] dataProvider3() {
    return new Object[][] {{"tc1"}, {"tc2"}, {"tc3"}};
  }

  @Test(retryAnalyzer = InvocationCountTest.class, dataProvider = "dataProvider")
  public void testAnnotationWithDataProvider(int paf, boolean test) {
    executionNumber++;
    if (paf == 2 && test == false) {
      if (r2 >= 1) {
        r2--;
        fail();
      }
    }
    if (paf == 4) {
      assertEquals(executionNumber, 5);
    }
  }

  @Test(retryAnalyzer = InvocationCountTest.class, dataProvider = "dataProvider2")
  public void testAnnotationWithDataProviderAndRecreateParameters(boolean dummy) {
    if (r3 == 1) {
      this.value = 0;
      r3--;
      fail();
    } else if (r3 == 0) {
      assertEquals(this.value, 42);
    }
  }

  @Test
  public void withFactory() {
    TestNG tng = new TestNG();
    tng.setTestClasses(new Class[] {MyFactory.class});
    FactoryTest.m_count = 0;

    tng.run();

    assertEquals(FactoryTest.m_count, 4);
  }

  @Override
  public boolean retry(ITestResult result) {
    invocations.add(result.getName());
    return retriesRemaining.getAndDecrement() >= 0;
  }

  public static class ThreeRetries implements IRetryAnalyzer {
    private final AtomicInteger remainingRetries = new AtomicInteger(3);

    @Override
    public boolean retry(ITestResult result) {
      invocations.add(result.getName());
      return remainingRetries.getAndDecrement() > 0;
    }
  }

  public static class RetryCountTest implements IRetryAnalyzer {
    private final AtomicInteger remainingRetries = new AtomicInteger(12);

    @Override
    public boolean retry(ITestResult result) {
      return remainingRetries.getAndDecrement() > 0;
    }
  }
}
