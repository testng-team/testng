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

    assertThat(TestListener.beforeInvocation)
        .containsEntry("test.listeners.github2522.FirstTestSamplefirstMethod", ITestResult.STARTED)
        .containsEntry("test.listeners.github2522.FirstTestSamplesecondMethod", ITestResult.STARTED)
        .containsEntry("test.listeners.github2522.FirstTestSamplethirdMethod", ITestResult.SKIP)
        .containsEntry("test.listeners.github2522.SecondTestSamplefirstMethod", ITestResult.STARTED)
        .containsEntry("test.listeners.github2522.SecondTestSamplesecondMethod", ITestResult.SKIP)
        .containsEntry("test.listeners.github2522.SecondTestSamplethirdMethod", ITestResult.SKIP);

    assertThat(TestListener.afterInvocation)
        .containsEntry("test.listeners.github2522.FirstTestSamplefirstMethod", ITestResult.SUCCESS)
        .containsEntry("test.listeners.github2522.FirstTestSamplesecondMethod", ITestResult.FAILURE)
        .containsEntry("test.listeners.github2522.FirstTestSamplethirdMethod", ITestResult.SKIP)
        .containsEntry("test.listeners.github2522.SecondTestSamplefirstMethod", ITestResult.SKIP)
        .containsEntry("test.listeners.github2522.SecondTestSamplesecondMethod", ITestResult.SKIP)
        .containsEntry("test.listeners.github2522.SecondTestSamplethirdMethod", ITestResult.SKIP);
  }

  @Test
  public void testSkip() {
    TestNG testNG = create(SkipTestSample.class);
    testNG.addListener(new SkipTestListener());
    testNG.run();

    assertThat(SkipTestSample.getFlag()).isTrue();
  }
}
