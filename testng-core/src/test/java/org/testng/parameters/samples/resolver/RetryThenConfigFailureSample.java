package org.testng.parameters.samples.resolver;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Two rows, a row re-read on retry, an {@code @AfterMethod} that fails once, and a provider that
 * breaks on its third call -- so the first row's second retry cannot rebuild its row. What the
 * second row then does tells whether the recorded configuration failure is still honored.
 */
public class RetryThenConfigFailureSample {

  public static class TwiceRetry implements IRetryAnalyzer {
    private final AtomicInteger retries = new AtomicInteger();

    @Override
    public boolean retry(ITestResult result) {
      return retries.incrementAndGet() <= 2;
    }
  }

  private final AtomicInteger providerCalls = new AtomicInteger();
  private final AtomicInteger afterCalls = new AtomicInteger();

  @DataProvider(name = "dp", cacheDataForTestRetries = false)
  public Object[][] dp() {
    if (providerCalls.incrementAndGet() > 2) {
      throw new IllegalStateException("provider broke on its third call");
    }
    return new Object[][] {{"first"}, {"second"}};
  }

  @AfterMethod
  public void after() {
    if (afterCalls.incrementAndGet() == 1) {
      throw new IllegalStateException("after method failed once");
    }
  }

  @Test(dataProvider = "dp", retryAnalyzer = TwiceRetry.class)
  public void test(String row) {
    ParameterRecorder.record("test", row);
    if ("first".equals(row)) {
      throw new AssertionError("first row always fails");
    }
  }
}
