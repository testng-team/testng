package test.dataprovider;

import org.testng.ITest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

@Test
public class IndicesFactorySample implements ITest {

    private final int value;
    private String testName;

    @Factory(dataProvider = "dp", indices = 1)
    public IndicesFactorySample(int value) {
        this.value = value;
    }

    @DataProvider(indices = {1,2})
    public static Object[][] dp() {
        return new Object[][]{
                new Object[]{1},
                new Object[]{2},
                new Object[]{3},
                new Object[]{4}
        };
    }

    @BeforeMethod
    public void setUp(Method method) {
        testName = method.getName().replace("test", "testName");
    }

    public void testA() {
    }

    public void testB() {
    }

    @Override
    public String getTestName() {
        return testName + "(" + value + ")";
    }
}
