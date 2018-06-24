package test.dataprovider;

import org.testng.annotations.Test;
import test.InvokedMethodNameListener;
import test.SimpleBaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class FailingDataProviderTest extends SimpleBaseTest {

  @Test(description = "TESTNG-142: Exceptions in DataProvider are not reported as failed test")
  public void failingDataProvider() {
    InvokedMethodNameListener listener = run(FailingDataProviderSample.class);

    assertThat(listener.getSkippedMethodNames()).containsExactly("dpThrowingException");
  }

  @Test(description = "TESTNG-447: Abort when two data providers have the same name")
  public void duplicateDataProviders() {
    InvokedMethodNameListener listener = run(DuplicateDataProviderSample.class);

    assertThat(listener.getFailedBeforeInvocationMethodNames()).containsExactly("f");
  }

  @Test
  public void failingDataProviderAndInvocationCount() {
    InvokedMethodNameListener listener = run(DataProviderWithErrorSample.class);

    assertThat(listener.getSkippedMethodNames())
        .containsExactly(
            "testShouldSkip",
            "testShouldSkip",
            "testShouldSkipEvenIfSuccessPercentage",
            "testShouldSkipEvenIfSuccessPercentage");
  }
}
