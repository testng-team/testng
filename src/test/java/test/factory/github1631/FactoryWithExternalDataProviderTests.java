package test.factory.github1631;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

public class FactoryWithExternalDataProviderTests {

    public FactoryWithExternalDataProviderTests() {
    }

    @Factory(dataProvider = "data", dataProviderClass = ExternalDataProviders.class)
    public FactoryWithExternalDataProviderTests(final int i) {
        // not important
    }

    @Test
    public void fakeTest() {
        // not important
    }
}
