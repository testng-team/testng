package test.invocationcount.issue3180;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicInteger;
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
      assertThat(counter.incrementAndGet()).isEqualTo(3);
    }
  }

  public static class TestWithNormalFailingTest {
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void alwaysFailingRegularTest() {
      fail();
    }
  }

  public static class TestWithSomeFailingIterations {

    @Test(
        dataProvider = "numbers",
        dataProviderClass = SampleTestContainer.class,
        retryAnalyzer = RetryAnalyzer.class)
    public void failsForOddNumbersOnly(int i) {
      assertThat(i % 2).isZero();
    }
  }

  public static class TestContainsAlwaysFailingDataDrivenTest {
    @Test(
        dataProvider = "atomicNumbers",
        dataProviderClass = SampleTestContainer.class,
        retryAnalyzer = RetryAnalyzer.class)
    public void alwaysFailingDataDrivenTest(AtomicInteger ignored) {
      fail();
    }
  }

  public static class TestContainsPercentageDrivenTest {
    private final AtomicInteger switcher = new AtomicInteger(1);

    @Test(invocationCount = 10, successPercentage = 90, retryAnalyzer = RetryAnalyzer.class)
    public void invocationCountTestWhichEventuallyPassesDueToSuccessFactors() {
      assertThat(switcher.getAndIncrement()).isLessThan(9);
    }
  }

  public static class TestContainsAllCombinations {
    private final AtomicInteger switcher = new AtomicInteger(1);

    @Test(
        dataProvider = "atomicNumbers",
        dataProviderClass = SampleTestContainer.class,
        retryAnalyzer = RetryAnalyzer.class)
    public void testDataProvider(AtomicInteger counter) {
      assertThat(counter.incrementAndGet()).isEqualTo(3);
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void alwaysFailingRegularTest() {
      fail();
    }

    @Test(
        dataProvider = "numbers",
        dataProviderClass = SampleTestContainer.class,
        retryAnalyzer = RetryAnalyzer.class)
    public void failsForOddNumbersOnly(int i) {
      assertThat(i % 2).isZero();
    }

    @Test(
        dataProvider = "atomicNumbers",
        dataProviderClass = SampleTestContainer.class,
        retryAnalyzer = RetryAnalyzer.class)
    public void alwaysFailingDataDrivenTest(AtomicInteger ignored) {
      fail();
    }

    @Test(invocationCount = 10, successPercentage = 90)
    public void invocationCountTestWhichEventuallyPassesDueToSuccessFactors() {
      assertThat(switcher.getAndIncrement()).isLessThan(9);
    }
  }
}
