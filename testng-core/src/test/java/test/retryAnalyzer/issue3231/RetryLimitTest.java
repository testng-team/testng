package test.retryAnalyzer.issue3231;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.Test;
import test.SimpleBaseTest;
import test.retryAnalyzer.issue3231.samples.RetryLimitSample;

public class RetryLimitTest extends SimpleBaseTest {

    @Test
    public void testEachRowHasItsOwnRetryLimit() {
        AtomicInteger invCount = new AtomicInteger(0);
        TestNG tng = create(RetryLimitSample.class);
        tng.addListener(new IInvokedMethodListener() {
            @Override
            public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
                if (method.isTestMethod()) {
                    invCount.incrementAndGet();
                }
            }
        });
        tng.run();
        // Each row: 1 original + 1 retry = 2 attempts.
        // 2 rows * 2 attempts = 4 total invocations.
        assertThat(invCount.get()).isEqualTo(4);
    }
}