package test.retryAnalyzer.dataprovider;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RetryAnalyzerWithoutDataProvider {
    private static int countWithoutDataProvider = 3;

    @Test(retryAnalyzer = DataProviderRetryAnalyzer.class)
    public void test() {
        Assert.assertTrue(countWithoutDataProvider-- == 0);
    }
}
