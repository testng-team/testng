package test.retryAnalyzer.issue3231.samples;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MutationSample {

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

    public static final AtomicInteger guardCounter = new AtomicInteger(0);

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
