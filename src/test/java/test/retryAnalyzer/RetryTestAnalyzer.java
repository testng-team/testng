package test.retryAnalyzer;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.util.RetryAnalyzerCount;

public class RetryTestAnalyzer {

  public static class TwiceRetryer extends RetryAnalyzerCount {

    public TwiceRetryer() {
      setCount(2);
    }

    @Override
    public boolean retryMethod(ITestResult result) {
      return true;
    }
  }

  public static class NoFail {

    public static int COUNT = 0;

    @Test(retryAnalyzer = TwiceRetryer.class)
    public void test() {
      COUNT++;
    }
  }

  public static class FailOnce {

    public static int COUNT = 0;
    private static boolean alreadyFailed = false;

    @Test(retryAnalyzer = TwiceRetryer.class)
    public void test() {
      COUNT++;
      if (!alreadyFailed) {
        alreadyFailed = true;
        Assert.fail();
      }
    }
  }

  public static class FourTestsAndSecondFailOnce {

    public static int COUNT = 0;
    public static int DATA_PROVIDER_COUNT = 0;
    private static boolean alreadyFailed = false;

    @DataProvider(name = "dataProvider")
    private Object[][] dataProvider() {
      DATA_PROVIDER_COUNT++;
      return new Object[][]{{"Test 1"}, {"Test 2"}, {"Test 3"}, {"Test 4"}};
    }

    @Test(retryAnalyzer = TwiceRetryer.class, dataProvider = "dataProvider")
    public void test(String name) {
      COUNT++;
      if ("Test 2".equals(name) && !alreadyFailed) {
        alreadyFailed = true;
        Assert.fail();
      }
    }
  }
}
