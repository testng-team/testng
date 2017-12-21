package test.factory.github1631;

import org.testng.*;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

public class GitHub1631Tests extends SimpleBaseTest {

    @Test(description = "Test @Factory(dataProvider) should implicitly inject data provider class name")
    public void factoryWithLocalDataProviderShouldUseHostClassName() {
        final DataProviderTransformer transformer = runTest(FactoryWithLocalDataProviderTests.class);
        Assert.assertEquals(transformer.getDataProviderClass(), FactoryWithLocalDataProviderTests.class);
    }

    @Test(description = "Test @Factory(dataProvider) should explicitly set data provider class name")
    public void factoryWithExplicitDataProviderShouldUseExternalClassName() {
        final DataProviderTransformer transformer = runTest(FactoryWithExternalDataProviderTests.class);
        Assert.assertEquals(transformer.getDataProviderClass(), ExternalDataProviders.class);
    }

    private DataProviderTransformer runTest(final Class<?> cls) {
        final TestNG tng = create(cls);
        final DataProviderTransformer dpt = new DataProviderTransformer();
        tng.addListener(dpt);
        tng.run();

        return dpt;
    }
}
