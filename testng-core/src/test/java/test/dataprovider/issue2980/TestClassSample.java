package test.dataprovider.issue2980;

import java.util.Arrays;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.collections.Pair;

public class TestClassSample {

  static final String THREAD_ID = "thread_id";

  @Test(dataProvider = "dp")
  public void testMethod(int ignored) {
    recordThreadId();
  }

  @Test(dataProvider = "dp1")
  public void anotherTestMethod(String ignored) {
    recordThreadId();
  }

  private static void recordThreadId() {
    ITestResult itr = Reporter.getCurrentTestResult();
    itr.setAttribute(
        THREAD_ID,
        new Pair<>(
            itr.getMethod().getMethodName() + "_" + Arrays.toString(itr.getParameters()),
            Thread.currentThread().getId()));
  }

  @DataProvider(name = "dp", parallel = true)
  public Object[][] getTestData() {
    return new Object[][] {{1}, {2}, {3}, {4}, {5}};
  }

  @DataProvider(name = "dp1", parallel = true)
  public Object[][] getTestData1() {
    return new Object[][] {{"A"}, {"B"}, {"C"}, {"D"}, {"E"}};
  }
}
