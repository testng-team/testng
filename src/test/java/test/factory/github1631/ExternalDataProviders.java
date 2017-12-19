package test.factory.github1631;

import org.testng.annotations.DataProvider;

public class ExternalDataProviders {

    @DataProvider
    public Object[] data() {
        return new Object[]{1, 2, 3};
    }
}
