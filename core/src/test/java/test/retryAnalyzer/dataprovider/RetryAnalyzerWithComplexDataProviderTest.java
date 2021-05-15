package test.retryAnalyzer.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class RetryAnalyzerWithComplexDataProviderTest extends SimpleBaseTest {
    private static int countWithStringArray = 3;
    private static int countWithObjectAndStringArray = 3;
    private static int countWithSingleParam = 3;
    private static int countWithoutDataProvider = 3;

    @DataProvider(name = "getTestData")
    public Object[][] getTestData() {
        return new String[][]{
                new String[]{"abc1", "cdf1"}};
    }

    @Test(dataProvider = "getTestData", description = "GITHUB - 2544", retryAnalyzer = DataProviderRetryAnalyzer.class)
    public void testWithStringArray(String... values) {
        System.out.println("Test Run " + values);
        Assert.assertTrue(countWithStringArray-- == 0);
    }

    @DataProvider(name = "getObjectData")
    public Object[][] getObjectData() {
        return new Object[][]{
                new Object[]{false, "abc1", "cdf1"}};
    }

    @Test(dataProvider = "getObjectData", description = "GITHUB - 2544", retryAnalyzer = DataProviderRetryAnalyzer.class)
    public void testWithObjectAndStringArray(boolean flag, String... values) {
        System.out.println("Test Run " + values + " with flag: " + flag);
        Assert.assertTrue(countWithObjectAndStringArray-- == 0);
    }

    @DataProvider(name = "getSingleParam", parallel = true)
    public Object[][] getSingleParam() {
        return new String[][]{
                new String[]{"abc"},
                new String[]{"bcd"}};
    }

    @Test(dataProvider = "getSingleParam", description = "GITHUB - 2544", retryAnalyzer = DataProviderRetryAnalyzer.class)
    public void testWithSingleParam(String value) {
        System.out.println("Test Run " + value);
        Assert.assertTrue(countWithSingleParam-- <= 0);
    }

    @Test(description = "GITHUB - 2544", retryAnalyzer = DataProviderRetryAnalyzer.class)
    public void testWithoutDataProvider() {
        Assert.assertTrue(countWithoutDataProvider-- == 0);
    }
}
