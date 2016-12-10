package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataProviderIntegrationSample {
    @DataProvider
    public Object[][] testInts() {
        return new Object[][] {
            new Object[] {new Integer(4)},
            new Object[] {new Integer(8)},
            new Object[] {new Integer(12)}};
    }

    @Test (dataProvider = "testInts", expectedExceptions = IllegalArgumentException.class)
    public void theTest(String aString) {
        Assert.assertNotNull(aString);
    }
}
