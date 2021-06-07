package test.listeners.github2522;

import org.testng.Assert;
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

    Assert.assertEquals(
        ITestResult.STARTED,
        TestListener.beforeInvocation
            .get("test.listeners.github2522.FirstTestSamplefirstMethod")
            .intValue());
    Assert.assertEquals(
        ITestResult.STARTED,
        TestListener.beforeInvocation
            .get("test.listeners.github2522.FirstTestSamplesecondMethod")
            .intValue());
    Assert.assertEquals(
        ITestResult.SKIP,
        TestListener.beforeInvocation
            .get("test.listeners.github2522.FirstTestSamplethirdMethod")
            .intValue());
    Assert.assertEquals(
        ITestResult.STARTED,
        TestListener.beforeInvocation
            .get("test.listeners.github2522.SecondTestSamplefirstMethod")
            .intValue());
    Assert.assertEquals(
        ITestResult.SKIP,
        TestListener.beforeInvocation
            .get("test.listeners.github2522.SecondTestSamplesecondMethod")
            .intValue());
    Assert.assertEquals(
        ITestResult.SKIP,
        TestListener.beforeInvocation
            .get("test.listeners.github2522.SecondTestSamplethirdMethod")
            .intValue());

    Assert.assertEquals(
        ITestResult.SUCCESS,
        TestListener.afterInvocation
            .get("test.listeners.github2522.FirstTestSamplefirstMethod")
            .intValue());
    Assert.assertEquals(
        ITestResult.FAILURE,
        TestListener.afterInvocation
            .get("test.listeners.github2522.FirstTestSamplesecondMethod")
            .intValue());
    Assert.assertEquals(
        ITestResult.SKIP,
        TestListener.afterInvocation
            .get("test.listeners.github2522.FirstTestSamplethirdMethod")
            .intValue());
    Assert.assertEquals(
        ITestResult.SKIP,
        TestListener.afterInvocation
            .get("test.listeners.github2522.SecondTestSamplefirstMethod")
            .intValue());
    Assert.assertEquals(
        ITestResult.SKIP,
        TestListener.afterInvocation
            .get("test.listeners.github2522.SecondTestSamplesecondMethod")
            .intValue());
    Assert.assertEquals(
        ITestResult.SKIP,
        TestListener.afterInvocation
            .get("test.listeners.github2522.SecondTestSamplethirdMethod")
            .intValue());
  }

  @Test
  public void testSkip() {
    TestNG testNG = create(SkipTestSample.class);
    testNG.addListener(new SkipTestListener());
    testNG.run();

    Assert.assertTrue(SkipTestSample.getFlag());
  }
}
