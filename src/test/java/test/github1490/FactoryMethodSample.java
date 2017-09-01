package test.github1490;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import test.listeners.github1490.DataProviderInfoProvider;

@Listeners(DataProviderInfoProvider.class)
public class FactoryMethodSample {
    private int i;

    @Factory(dataProvider = "dp")
    public static Object[] newInstance(int i) {
        FactoryMethodSample[] object = new FactoryMethodSample[] {new FactoryMethodSample()};
        object[0].i = i;
        return object;
    }

    @DataProvider(name = "dp")
    public static Object[][] getData() {
        return new Object[][]{
                {1},
                {2}
        };
    }

    @Test
    public void testMethod() {
        Assert.assertTrue(i > 0);
    }
}
