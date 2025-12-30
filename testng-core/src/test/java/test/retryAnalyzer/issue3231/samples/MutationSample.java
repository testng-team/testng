package test.retryAnalyzer.issue3231.samples;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.Assert;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MutationSample {

    public static class Custom {
        private UUID id;
        public Custom(UUID id) { this.id = id; }
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Custom custom = (Custom) o;
            return Objects.equals(id, custom.id);
        }
        @Override
        public int hashCode() { return Objects.hash(id); }
    }

    public static class MyRetry implements IRetryAnalyzer {
        private int retryCount = 0;
        private int maxRetryCount = 3;
        @Override
        public boolean retry(ITestResult result) {
            if (retryCount < maxRetryCount) {
                retryCount++;
                return true;
            }
            return false;
        }
    }

    public static final AtomicInteger guardCounter = new AtomicInteger(0);

    @DataProvider(name = "dpCustomObject")
    public Object[][] dpCustomObject() {
        return new Object[][] {
            { new Custom(UUID.randomUUID()) }
        };
    }

    @Test(dataProvider = "dpCustomObject", retryAnalyzer = MyRetry.class)
    public void willNotStopAfter3FailuresCustom(Custom newObject) {
        if (guardCounter.incrementAndGet() > 100) {
            return; 
        }
        newObject.setId(UUID.randomUUID()); // Mutate!
        Assert.fail();
    }
}