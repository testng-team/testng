package test.retryAnalyzer.issue3231;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
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

public class Issue3231Test extends SimpleBaseTest {

    public static class MutableObject {
        private int id;
        public MutableObject(int id) { this.id = id; }
        public void setId(int id) { this.id = id; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MutableObject that = (MutableObject) o;
            return id == that.id;
        }
        @Override
        public int hashCode() { return Objects.hash(id); }
        @Override
        public String toString() { return "MutableObject{id=" + id + "}"; }
    }

    public static class MyRetry implements IRetryAnalyzer {
        private int count = 0;
        @Override
        public boolean retry(ITestResult result) {
            count++;
            return count < 3; // Should retry max 2 times (total 3 attempts)
        }
    }

    public static class SampleTest {
        static final AtomicInteger guardCounter = new AtomicInteger(0);

        @DataProvider(name = "dp")
        public Object[][] dp() {
            return new Object[][] {
                { new MutableObject(1) }
            };
        }

        @Test(dataProvider = "dp", retryAnalyzer = MyRetry.class)
        public void test(MutableObject obj) {
            if (guardCounter.incrementAndGet() > 100) {
                return; 
            }
            obj.setId(obj.id + 1); // Mutate!
            Assert.fail("fail");
        }
    }

    @Test
    public void testMutationDoesNotCauseInfiniteRetryLoop() {
        SampleTest.guardCounter.set(0);
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
        assertThat(invCount.get()).isEqualTo(3);
    }
}