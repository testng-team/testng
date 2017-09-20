package test.parameters;

import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class Issue1061Sample {
    private final long timeout;
    private final long waitTime;

    @DataProvider
    public static Object[][] dp() {
        return new Object[][]{
                new Object[]{1_000, 2_000},
                new Object[]{3_000, 6_000}
        };
    }

    @Factory(dataProvider = "dp")
    public Issue1061Sample(long timeout, long waitTime) {
        this.timeout = timeout;
        this.waitTime = waitTime;
    }

    @BeforeMethod
    public void setup(ITestResult result) {
        result.getMethod().setTimeOut(timeout);
    }

    @Test
    public void test() throws InterruptedException {
        Thread.sleep(waitTime);
    }
}