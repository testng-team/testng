package test.dataprovider.retry;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RetryAnalyzerTest {

    @DataProvider(name = "getTestData")
    public Object[][] getTestData() {
        return new String[][]{
                new String[]{"abc1", "cdf1"}};
    }

    @Test(dataProvider = "getTestData", retryAnalyzer = RetryAnalyzer.class)
    public void test(String... values) {
        System.out.println("Test Run " + values);
        Assert.assertTrue(true);
    }
}
