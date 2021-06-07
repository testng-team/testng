package test.dataprovider.issue2157;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestClassWithDataProviderThatThrowsExceptions {

  @Test(dataProvider = "dp", retryAnalyzer = SimplyRetry.class)
  public void testMethod(String i) {
    if ("First".equalsIgnoreCase(i) || "Second".equalsIgnoreCase(i)) {
      Assert.fail();
    }
  }

  private static AtomicInteger counter = new AtomicInteger();

  @DataProvider(name = "dp")
  public static Object[][] dpWithException() {
    return new Object[][] {
      {foo()},
    };
  }

  private static String foo() {
    counter.getAndIncrement();

    if (counter.get() == 1) {
      return "First";
    }
    if (counter.get() == 2) {
      return "Second";
    }
    throw new RuntimeException("TestNG doesn't handle an exception");
  }

  public static class SimplyRetry implements IRetryAnalyzer {

    private static int attempts = 1;

    @Override
    public boolean retry(ITestResult result) {
      return attempts++ != 3;
    }
  }
}
