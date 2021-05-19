package test.retryAnalyzer.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ComplexDataProviderSample {
    private static int countWithStringArray = 3;
    private static int countWithObjectAndStringArrayForSuccess = 3;
    private static int countWithObjectAndStringArrayForFailure = 3;
    private static int countWithSingleParam = 3;
    private static int countWithoutDataProvider = 3;

    @DataProvider(name = "getTestData")
    public Object[][] getTestData() {
        return new String[][]{
                new String[]{"abc1", "cdf1"}};
    }

    @Test(dataProvider = "getTestData", retryAnalyzer = DataProviderRetryAnalyzer.class)
    public void testWithStringArray(String... values) {
        Assert.assertTrue(countWithStringArray-- == 0, "Test execution is not" +
                "successful after 3 retry attempts configured in retryAnalyzer for this data " + values);
    }

    @DataProvider(name = "getObjectData")
    public Object[][] getObjectData() {
        return new Object[][]{
                new Object[]{false, "abc1", "cdf1"}};
    }

    @Test(dataProvider = "getObjectData", retryAnalyzer = DataProviderRetryAnalyzer.class)
    public void testWithObjectAndStringArraySuccessAfterAttempts(boolean flag, String... values) {
        Assert.assertTrue(countWithObjectAndStringArrayForSuccess-- == 0, "Test execution is not" +
                "successful after 3 retry attempts configured in retryAnalyzer for this data " + values +
                "with boolean flag as " + flag);
    }

    @Test(dataProvider = "getObjectData", retryAnalyzer = DataProviderRetryAnalyzer.class)
    public void testWithObjectAndStringArrayFailedAfterAttempts(boolean flag, String... values) {
        Assert.assertTrue(countWithObjectAndStringArrayForFailure-- < 0, "Test execution is not" +
                "successful after 3 retry attempts configured in retryAnalyzer for this data " + values +
                "with boolean flag as " + flag);
    }

    @DataProvider(name = "getSingleParam")
    public Object[][] getSingleParam() {
        return new Integer[][]{
                new Integer[]{0},
                new Integer[]{0}};
    }

    @Test(dataProvider = "getSingleParam", retryAnalyzer = DataProviderRetryAnalyzer.class)
    public void testWithSingleParam(int count) {
        Assert.assertTrue(countWithSingleParam-- <= 0, "Test execution is not" +
                "successful after 3 retry attempts configured in retryAnalyzer for this data " + count);
    }

    @Test(retryAnalyzer = DataProviderRetryAnalyzer.class)
    public void testWithoutDataProvider() {
        Assert.assertTrue(countWithoutDataProvider-- == 0);
    }
}
