package test.invocationcount;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.invocationcount.issue1719.IssueTest;
import test.invocationcount.issue3170.DataDrivenWithSuccessPercentageAndInvocationCountDefinedSample;
import test.invocationcount.issue3170.DataDrivenWithSuccessPercentageDefinedSample;

public class FailedInvocationCountTest extends SimpleBaseTest {

  private void runTest(boolean skip, int passed, int failed, int skipped) {
    TestNG testng = create(FailedInvocationCount.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.setSkipFailedInvocationCounts(skip);
    testng.addListener(tla);
    testng.run();

    Assert.assertEquals(tla.getPassedTests().size(), passed);
    Assert.assertEquals(tla.getFailedTests().size(), failed);
    Assert.assertEquals(tla.getSkippedTests().size(), skipped);
  }

  @Test
  public void verifyGloballyShouldStop() {
    runTest(true, 4, 1, 5);
  }

  @Test
  public void verifyGloballyShouldNotStop() {
    runTest(false, 4, 6, 0);
  }

  @Test
  public void verifyAttributeShouldStop() {
    TestNG testng = create(FailedInvocationCount2.class);
    TestListenerAdapter tla = new TestListenerAdapter();
    testng.addListener(tla);
    testng.run();

    Assert.assertEquals(tla.getPassedTests().size(), 8);
    Assert.assertEquals(tla.getFailedTests().size(), 7);
    Assert.assertEquals(tla.getSkippedTests().size(), 5);
  }

  @Test(dataProvider = "dp")
  public void ensureSuccessPercentageWorksFineWith(Class<?> clazz, IssueTest.Expected expected) {
    TestNG testng = create(clazz);
    AtomicInteger failed = new AtomicInteger(0);
    AtomicInteger passed = new AtomicInteger(0);
    AtomicInteger failedWithInSuccessPercentage = new AtomicInteger(0);
    testng.addListener(
        new IInvokedMethodListener() {
          @Override
          public void afterInvocation(IInvokedMethod method, ITestResult testResult) {

            switch (testResult.getStatus()) {
              case ITestResult.SUCCESS:
                passed.incrementAndGet();
                break;
              case ITestResult.FAILURE:
                failed.incrementAndGet();
                break;
              case ITestResult.SUCCESS_PERCENTAGE_FAILURE:
                failedWithInSuccessPercentage.incrementAndGet();
                break;
              default:
            }
          }
        });
    testng.run();
    assertThat(passed.get()).isEqualTo(expected.success());
    assertThat(failed.get()).isEqualTo(expected.failures());
    assertThat(failedWithInSuccessPercentage.get())
        .isEqualTo(expected.failedWithinSuccessPercentage());
  }

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {
      {
        DataDrivenWithSuccessPercentageAndInvocationCountDefinedSample.class,
        new IssueTest.Expected().failures(10)
      },
      {
        DataDrivenWithSuccessPercentageDefinedSample.class,
        new IssueTest.Expected().failures(3).success(1)
      }
    };
  }
}
