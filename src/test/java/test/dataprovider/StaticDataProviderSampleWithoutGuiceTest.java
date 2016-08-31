package test.dataprovider;

import org.testng.Assert;
import org.testng.annotations.Test;

public class StaticDataProviderSampleWithoutGuiceTest {

    @Test(dataProvider = "static", dataProviderClass = StaticProvider.class)
    public void verifyStatic(String s) {
        Assert.assertEquals(s, "Cedric");
    }

    @Test(dataProvider = "external", dataProviderClass = NonStaticProvider.class)
    public void verifyExternal(String s) {
        Assert.assertEquals(s, "Cedric");
    }
}
