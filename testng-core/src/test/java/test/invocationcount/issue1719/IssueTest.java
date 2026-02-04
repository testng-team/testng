package test.invocationcount.issue1719;

import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test(dataProvider = "dp")
  public void testSuccessPercentageCalculation(Class<?> clazz, Expected expected) {
    TestNG testng = create(clazz);
    DummyReporter listener = new DummyReporter();
    testng.addListener(listener);
    testng.run();
    SoftAssertions assertions = new SoftAssertions();
    String msg =
        pretty(
            "[failedWithinSuccessPercentage]",
            expected.failedWithinSuccessPercentage,
            listener.getFailedWithinSuccessPercentage());
    assertions
        .assertThat(listener.getFailedWithinSuccessPercentage())
        .withFailMessage(msg)
        .hasSize(expected.failedWithinSuccessPercentage);

    msg = pretty("[skip]", expected.skip, listener.getSkip());
    assertions.assertThat(listener.getSkip()).withFailMessage(msg).hasSize(expected.skip);

    msg = pretty("[success]", expected.success, listener.getSuccess());
    assertions.assertThat(listener.getSuccess()).withFailMessage(msg).hasSize(expected.success);

    msg = pretty("[failures]", expected.failures, listener.getFailures());
    assertions.assertThat(listener.getFailures()).withFailMessage(msg).hasSize(expected.failures);

    assertions.assertAll();
  }

  private static String pretty(String prefix, int expected, Set<ITestResult> actual) {
    return prefix + " test. Expected: " + expected + ", Actual: " + actual.size();
  }

  @DataProvider(name = "dp")
  public Object[][] dp() {
    return new Object[][] {
      {
        TestclassSample.DataDrivenTestHavingZeroSuccessPercentageAndNoInvocationCount.class,
        new Expected().failures(2)
      },
      {
        TestclassSample.DataDrivenTestHavingValidSuccessPercentageAndInvocationCount.class,
        // Parameter - Invocation Count - Expected Test Result
        // .  1             1               Failed Within success percentage (30% expected)
        //    1             2               Passed (Remember this is a flaky test simulation)
        //    2             1               Failed Within success percentage (30% expected)
        //    2             2               Failed
        new Expected().failures(1).failedWithinSuccessPercentage(2).success(1)
      },
      {
        TestclassSample.RegularTestWithZeroSuccessPercentage.class,
        new Expected().failedWithinSuccessPercentage(1)
      },
      {
        TestclassSample.RegularTestWithZeroSuccessPercentageAndInvocationCount.class,
        new Expected().failedWithinSuccessPercentage(2)
      },
    };
  }

  public static class Expected {
    private int failures = 0;
    private int success = 0;
    private int skip = 0;
    private int failedWithinSuccessPercentage = 0;

    public int failures() {
      return failures;
    }

    public Expected failures(int failures) {
      this.failures = failures;
      return this;
    }

    public int success() {
      return success;
    }

    public Expected success(int success) {
      this.success = success;
      return this;
    }

    public int skip() {
      return skip;
    }

    public Expected skip(int skip) {
      this.skip = skip;
      return this;
    }

    public int failedWithinSuccessPercentage() {
      return failedWithinSuccessPercentage;
    }

    public Expected failedWithinSuccessPercentage(int failedWithinSuccessPercentage) {
      this.failedWithinSuccessPercentage = failedWithinSuccessPercentage;
      return this;
    }

    @Override
    public String toString() {
      return "{failures="
          + failures
          + ", success="
          + success
          + ", skip="
          + skip
          + ", failedWithinSuccessPercentage="
          + failedWithinSuccessPercentage
          + '}';
    }
  }
}
