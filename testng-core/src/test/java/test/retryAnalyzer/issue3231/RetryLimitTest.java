package test.retryAnalyzer.issue3231;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class RetryLimitTest extends SimpleBaseTest {

    public static class MyRetry implements IRetryAnalyzer {
        private int count = 0;
        @Override
        public boolean retry(ITestResult result) {
            count++;
            return count < 2; // Retry once (total 2 attempts)
        }
    }

    public static class SampleTest {
        @DataProvider(name = "dp")
        public Object[][] dp() {
            return new Object[][] {
                { "a" },
                { "a" }
            };
        }

        @Test(dataProvider = "dp", retryAnalyzer = MyRetry.class)
        public void test(String s) {
            Assert.fail("fail");
        }
    }

    @Test
    public void testEachRowHasItsOwnRetryLimit() {
        AtomicInteger invCount = new AtomicInteger(0);
        TestNG tng = create(SampleTest.class);
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
        // Previously it was 3 because Row 2 shared and exhausted Row 1's counter.
        assertThat(invCount.get()).isEqualTo(4);
    }
}
