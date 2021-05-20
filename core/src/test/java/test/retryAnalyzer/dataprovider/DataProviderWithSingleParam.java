package test.retryAnalyzer.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderWithSingleParam {
    private static int countWithSingleParam = 3;

    @DataProvider(name = "getSingleParam", parallel = true)
    public Object[][] getSingleParam() {
        return new Integer[][]{
                new Integer[]{0},
                new Integer[]{0}};
    }

    @Test(dataProvider = "getSingleParam", retryAnalyzer = DataProviderRetryAnalyzer.class)
    public void test(int count) {
        Assert.assertTrue(countWithSingleParam-- <= 0, "Test execution is not" +
                "successful after 3 retry attempts configured in retryAnalyzer for this data " + count);
    }
}
