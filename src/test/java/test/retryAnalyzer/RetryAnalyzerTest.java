package test.retryAnalyzer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.testng.ITestNGListener;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import test.SimpleBaseTest;
import test.retryAnalyzer.github1519.MyListener;
import test.retryAnalyzer.github1519.TestClassSample;

public class RetryAnalyzerTest extends SimpleBaseTest {
    @Test
    public void testInvocationCounts() {
        TestNG tng = create(InvocationCountTest.class);
        TestListenerAdapter tla = new TestListenerAdapter();
        tng.addListener((ITestNGListener) new TestResultPruner());
        tng.addListener((ITestNGListener) tla);

        tng.run();

        assertThat(tla.getFailedTests()).isEmpty();

        List<ITestResult> fsp = tla.getFailedButWithinSuccessPercentageTests();
        assertThat(fsp).hasSize(1);
        assertThat(fsp.get(0).getName()).isEqualTo("failAfterThreeRetries");

        List<ITestResult> skipped = tla.getSkippedTests();
        assertThat(skipped).hasSize(InvocationCountTest.invocations.size() - fsp.size());
    }

    @Test
    public void testIfRetryIsInvokedBeforeListener() {
        TestNG tng = create(TestClassSample.class);
        tng.addListener((ITestNGListener)new MyListener());
        tng.run();
        assertThat(TestClassSample.messages).containsExactly("afterInvocation", "retry", "afterInvocation");
    }
}
