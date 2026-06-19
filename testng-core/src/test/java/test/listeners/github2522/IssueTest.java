package test.listeners.github2522;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {

  @Test
  public void issueTest() {
    TestNG testNG = create(FirstTestSample.class, SecondTestSample.class);
    testNG.addListener(new TestListener());
    testNG.run();

    assertThat(ITestResult.STARTED)
        .isEqualTo(
            TestListener.beforeInvocation
                .get("test.listeners.github2522.FirstTestSamplefirstMethod")
                .intValue());
    assertThat(ITestResult.STARTED)
        .isEqualTo(
            TestListener.beforeInvocation
                .get("test.listeners.github2522.FirstTestSamplesecondMethod")
                .intValue());
    assertThat(ITestResult.SKIP)
        .isEqualTo(
            TestListener.beforeInvocation
                .get("test.listeners.github2522.FirstTestSamplethirdMethod")
                .intValue());
    assertThat(ITestResult.STARTED)
        .isEqualTo(
            TestListener.beforeInvocation
                .get("test.listeners.github2522.SecondTestSamplefirstMethod")
                .intValue());
    assertThat(ITestResult.SKIP)
        .isEqualTo(
            TestListener.beforeInvocation
                .get("test.listeners.github2522.SecondTestSamplesecondMethod")
                .intValue());
    assertThat(ITestResult.SKIP)
        .isEqualTo(
            TestListener.beforeInvocation
                .get("test.listeners.github2522.SecondTestSamplethirdMethod")
                .intValue());

    assertThat(ITestResult.SUCCESS)
        .isEqualTo(
            TestListener.afterInvocation
                .get("test.listeners.github2522.FirstTestSamplefirstMethod")
                .intValue());
    assertThat(ITestResult.FAILURE)
        .isEqualTo(
            TestListener.afterInvocation
                .get("test.listeners.github2522.FirstTestSamplesecondMethod")
                .intValue());
    assertThat(ITestResult.SKIP)
        .isEqualTo(
            TestListener.afterInvocation
                .get("test.listeners.github2522.FirstTestSamplethirdMethod")
                .intValue());
    assertThat(ITestResult.SKIP)
        .isEqualTo(
            TestListener.afterInvocation
                .get("test.listeners.github2522.SecondTestSamplefirstMethod")
                .intValue());
    assertThat(ITestResult.SKIP)
        .isEqualTo(
            TestListener.afterInvocation
                .get("test.listeners.github2522.SecondTestSamplesecondMethod")
                .intValue());
    assertThat(ITestResult.SKIP)
        .isEqualTo(
            TestListener.afterInvocation
                .get("test.listeners.github2522.SecondTestSamplethirdMethod")
                .intValue());
  }

  @Test
  public void testSkip() {
    TestNG testNG = create(SkipTestSample.class);
    testNG.addListener(new SkipTestListener());
    testNG.run();

    assertThat(SkipTestSample.getFlag()).isTrue();
  }
}
