package org.testng.conffailure.issue3616;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;
import org.testng.conffailure.samples.issue3616.FailingSetupSample;
import org.testng.conffailure.samples.issue3616.SkippingSetupSample;
import test.SimpleBaseTest;

/**
 * A test that a configuration skip of its own class skips gets the exception of that skip. A
 * configuration failure in another class of the same {@code <test>} must not replace it.
 */
public class Issue3616Test extends SimpleBaseTest {

  @Test(description = "GITHUB-3616")
  public void aFailureInAnotherClassDoesNotHideTheOwnConfigurationSkip() {
    TestListenerAdapter tla = runAndListen(FailingSetupSample.class, SkippingSetupSample.class);

    assertThat(skippedTestOf(tla, SkippingSetupSample.class).getThrowable())
        .isInstanceOf(SkipException.class)
        .hasMessage("SkippingSetupSample setup skipped");
    assertThat(skippedTestOf(tla, FailingSetupSample.class).getThrowable())
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("FailingSetupSample setup failed");
  }

  @Test(description = "GITHUB-3616")
  public void theOwnConfigurationSkipAloneStillGivesItsException() {
    TestListenerAdapter tla = runAndListen(SkippingSetupSample.class);

    assertThat(skippedTestOf(tla, SkippingSetupSample.class).getThrowable())
        .isInstanceOf(SkipException.class)
        .hasMessage("SkippingSetupSample setup skipped");
  }

  private static TestListenerAdapter runAndListen(Class<?>... classes) {
    TestListenerAdapter tla = new TestListenerAdapter();
    TestNG testng = create(classes);
    testng.addListener(tla);
    testng.run();
    return tla;
  }

  private static ITestResult skippedTestOf(TestListenerAdapter tla, Class<?> testClass) {
    return tla.getSkippedTests().stream()
        .filter(result -> result.getTestClass().getRealClass() == testClass)
        .findFirst()
        .orElseThrow();
  }
}
