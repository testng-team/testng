package test.retryAnalyzer.github1706;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class DataDrivenSample {
    private AtomicInteger counter = new AtomicInteger(0);

    @Test(retryAnalyzer = LocalRetry.class, dataProvider = "getdata")
    public void testMethod(int i) {
        Assert.assertTrue( i > 0);
        if (counter.incrementAndGet() != 3) {
            Assert.fail();
        }
        counter = new AtomicInteger(0);
    }

    @DataProvider(name = "getdata")
    public Object[][] getData() {
        return new Object[][]{
                {1},
                {2}
        };
    }

}
