package test.retryAnalyzer;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import test.retryAnalyzer.github857.GitHub857Listener;
import test.retryAnalyzer.github857.GitHub857Sample;

public class RetryAnalyzerTest extends SimpleBaseTest {
    @Test
    public void testInvocationCounts() {
        TestNG tng = create(InvocationCountTest.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        tng.addListener(tla);

        tng.run();

        assertFalse(tng.hasFailure());
        assertFalse(tng.hasSkip());

        assertTrue(tla.getFailedTests().isEmpty());

        List<ITestResult> fsp = tla.getFailedButWithinSuccessPercentageTests();
        assertEquals(fsp.size(), 1);
        assertEquals(fsp.get(0).getName(), "failAfterThreeRetries");

        List<ITestResult> skipped = tla.getSkippedTests();
        assertEquals(skipped.size(), InvocationCountTest.invocations.size() - fsp.size());
    }

    @Test(description = "GITHUB-857: onTestFailure not being called when test is retried")
    public void onTestFailureShouldBeCalledWhenTestIsRetried() {
        TestNG tng = create(GitHub857Sample.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        tng.addListener(tla);

        tng.run();

        assertEquals(tla.getPassedTests().size(), 0);
        assertEquals(tla.getFailedButWithinSuccessPercentageTests().size(), 0);
        assertEquals(tla.getSkippedTests().size(), 1);
        assertEquals(tla.getSkippedTests().get(0).getMethod().getMethodName(), "test");
        assertEquals(tla.getFailedTests().size(), 1);
        assertEquals(tla.getFailedTests().get(0).getMethod().getMethodName(), "test");

        assertEquals(GitHub857Listener.passedTests.size(), 0);
        assertEquals(GitHub857Listener.failedButWSPerTests.size(), 0);
        assertEquals(GitHub857Listener.skippedTests.size(), 1);
        assertEquals(GitHub857Listener.skippedTests.get(0).getMethod().getMethodName(), "test");
        assertEquals(GitHub857Listener.failedTests.size(), 1);
        assertEquals(GitHub857Listener.failedTests.get(0).getMethod().getMethodName(), "test");
    }
}
