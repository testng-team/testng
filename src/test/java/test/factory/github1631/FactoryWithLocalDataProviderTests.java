package test.factory.github1631;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryWithLocalDataProviderTests {

    public FactoryWithLocalDataProviderTests() {
    }

    @Factory(dataProvider = "data")
    public FactoryWithLocalDataProviderTests(final int i) {
        // not important
    }

    @DataProvider
    public Object[] data() {
        return new Object[]{1, 2, 3};
    }

    @Test
    public void fakeTest() {
        // not important
    }
}
