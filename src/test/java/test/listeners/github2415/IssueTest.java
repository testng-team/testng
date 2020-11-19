package test.listeners.github2415;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class IssueTest extends SimpleBaseTest {
    @Test
    public void issueTest() {
        TestNG testNG = create(FirstTestClass.class, SecondTestClass.class);
        testNG.addListener(new SkipAfterPreviousTestClassFailureListener());
        testNG.run();

        Assert.assertEquals(ITestResult.SUCCESS, SkipAfterPreviousTestClassFailureListener.status.get("test.listeners.github2415.FirstTestClassfirstMethod").intValue());
        Assert.assertEquals(ITestResult.FAILURE, SkipAfterPreviousTestClassFailureListener.status.get("test.listeners.github2415.FirstTestClasssecondMethod").intValue());
        Assert.assertEquals(ITestResult.SKIP, SkipAfterPreviousTestClassFailureListener.status.get("test.listeners.github2415.FirstTestClassthirdMethod").intValue());
        Assert.assertEquals(ITestResult.SKIP, SkipAfterPreviousTestClassFailureListener.status.get("test.listeners.github2415.SecondTestClassfirstMethod").intValue());
        Assert.assertEquals(ITestResult.SKIP, SkipAfterPreviousTestClassFailureListener.status.get("test.listeners.github2415.SecondTestClasssecondMethod").intValue());
        Assert.assertEquals(ITestResult.SKIP, SkipAfterPreviousTestClassFailureListener.status.get("test.listeners.github2415.SecondTestClassthirdMethod").intValue());

    }
}
