package test.timeout.github2672;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Throwables;
import java.util.Arrays;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.internal.thread.ThreadTimeoutException;

public class TimeoutStacktraceTest {

  private static class TimeoutStacktraceTestListener implements ITestListener {
    private Throwable testError = null;

    @Override
    public void onTestSuccess(ITestResult result) {
      testError = result.getThrowable();
    }

    public Throwable getTestError() {
      return testError;
    }
  }

  @Test
  public void verifyTimeoutStacktraceNewExecutor() {
    TestNG testng = new TestNG();
    testng.setTestClasses(new Class[] {TimeoutStacktraceTestSample.class});
    TimeoutStacktraceTestListener listener = new TimeoutStacktraceTestListener();
    testng.addListener(listener);

    testng.run();

    Throwable testError = listener.getTestError();
    assertThat((testError instanceof ThreadTimeoutException)).isTrue();
    assertThat(
            Arrays.stream(testError.getStackTrace())
                .anyMatch(s -> s.getMethodName().equals("testTimeoutStacktrace")))
        .isTrue();
  }

  @Test
  public void verifyTimeoutStacktraceNoExecutor() {
    TestNG testng = new TestNG();
    testng.setTestClasses(new Class[] {TimeoutStacktraceTestSample.class});
    TimeoutStacktraceTestListener listener = new TimeoutStacktraceTestListener();
    testng.addListener(listener);
    testng.setSuiteThreadPoolSize(2);
    testng.run();

    Throwable testError = listener.getTestError();
    assertThat((testError instanceof ThreadTimeoutException)).isTrue();
    assertThat(
            Throwables.getCausalChain(testError).stream()
                .flatMap((Throwable throwable) -> Arrays.stream(throwable.getStackTrace()))
                .anyMatch(s -> s.getMethodName().equals("testTimeoutStacktrace")))
        .isTrue();
  }
}
