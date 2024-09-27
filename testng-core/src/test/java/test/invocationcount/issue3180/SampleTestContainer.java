package test.invocationcount.issue3180;

import static org.testng.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SampleTestContainer {
  @DataProvider
  public static Object[][] atomicNumbers() {
    return new Object[][] {{new AtomicInteger(0)}};
  }

  @DataProvider
  public static Object[][] numbers() {
    return new Object[][] {{1}, {2}, {3}};
  }

  public static class TestContainsFlakyDataDrivenTest {

    @Test(
        dataProvider = "atomicNumbers",
        dataProviderClass = SampleTestContainer.class,
        retryAnalyzer = RetryAnalyzer.class)
    public void testDataProvider(AtomicInteger counter) {
      assertEquals(counter.incrementAndGet(), 3);
    }
  }

  public static class TestWithNormalFailingTest {
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void alwaysFailingRegularTest() {
      Assert.fail();
    }
  }

  public static class TestWithSomeFailingIterations {

    @Test(
        dataProvider = "numbers",
        dataProviderClass = SampleTestContainer.class,
        retryAnalyzer = RetryAnalyzer.class)
    public void failsForOddNumbersOnly(int i) {
      assertEquals(i % 2, 0);
    }
  }

  public static class TestContainsAlwaysFailingDataDrivenTest {
    @Test(
        dataProvider = "atomicNumbers",
        dataProviderClass = SampleTestContainer.class,
        retryAnalyzer = RetryAnalyzer.class)
    public void alwaysFailingDataDrivenTest(AtomicInteger ignored) {
      Assert.fail();
    }
  }

  public static class TestContainsPercentageDrivenTest {
    private final AtomicInteger switcher = new AtomicInteger(1);

    @Test(invocationCount = 10, successPercentage = 90, retryAnalyzer = RetryAnalyzer.class)
    public void invocationCountTestWhichEventuallyPassesDueToSuccessFactors() {
      Assert.assertTrue(switcher.getAndIncrement() < 9);
    }
  }

  public static class TestContainsAllCombinations {
    private final AtomicInteger switcher = new AtomicInteger(1);

    @Test(
        dataProvider = "atomicNumbers",
        dataProviderClass = SampleTestContainer.class,
        retryAnalyzer = RetryAnalyzer.class)
    public void testDataProvider(AtomicInteger counter) {
      assertEquals(counter.incrementAndGet(), 3);
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void alwaysFailingRegularTest() {
      Assert.fail();
    }

    @Test(
        dataProvider = "numbers",
        dataProviderClass = SampleTestContainer.class,
        retryAnalyzer = RetryAnalyzer.class)
    public void failsForOddNumbersOnly(int i) {
      assertEquals(i % 2, 0);
    }

    @Test(
        dataProvider = "atomicNumbers",
        dataProviderClass = SampleTestContainer.class,
        retryAnalyzer = RetryAnalyzer.class)
    public void alwaysFailingDataDrivenTest(AtomicInteger ignored) {
      Assert.fail();
    }

    @Test(invocationCount = 10, successPercentage = 90)
    public void invocationCountTestWhichEventuallyPassesDueToSuccessFactors() {
      Assert.assertTrue(switcher.getAndIncrement() < 9);
    }
  }
}
