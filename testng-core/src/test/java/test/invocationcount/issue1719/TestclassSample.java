package test.invocationcount.issue1719;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestclassSample {

  @DataProvider(name = "dp")
  public static Object[][] getData() {
    return new Object[][] {{1}, {2}};
  }

  public static class DataDrivenTestHavingZeroSuccessPercentageAndNoInvocationCount {
    @Test(successPercentage = 0, dataProvider = "dp", dataProviderClass = TestclassSample.class)
    public void dataDrivenTestMethod(int i) {
      Assert.fail("Failing iteration:" + i);
    }
  }

  public static class DataDrivenTestHavingValidSuccessPercentageAndInvocationCount {

    private boolean shouldFail = true;
    private Map<Integer, AtomicInteger> tracker = new HashMap<>();

    @Test(
        successPercentage = 30,
        dataProvider = "dp",
        invocationCount = 2,
        dataProviderClass = TestclassSample.class)
    public void dataDrivenTestMethodWithInvocationCount(int i) {
      int current = tracker.computeIfAbsent(i, k -> new AtomicInteger()).incrementAndGet();
      String msg = String.format("Parameter [%d], Invocation [%d]", i, current);
      if (i != 1) { // If the parameter is NOT 1, then just fail
        Assert.fail("Failing test " + msg);
      }
      if (shouldFail) { // If the parameter is 1, then simulate a flaky test that passes and fails
        shouldFail = false;
        Assert.fail("Failing test " + msg);
      }
    }
  }

  public static class RegularTestWithZeroSuccessPercentage {
    @Test(successPercentage = 0)
    public void simpleTestMethod() {
      Assert.fail();
    }
  }

  public static class RegularTestWithZeroSuccessPercentageAndInvocationCount {
    @Test(successPercentage = 0, invocationCount = 2)
    public void testMethodWithMultipleInvocations() {
      Assert.fail();
    }
  }
}
