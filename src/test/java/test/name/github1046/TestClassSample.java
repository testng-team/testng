package test.name.github1046;

import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotEquals;

public class TestClassSample implements IHookable {

    @DataProvider (name = "dp", parallel = true)
    public Object[][] getTestData() {
        return new Object[][] {
            {1}, {2}, {3}, {4}, {5}
        };
    }

    @Test (dataProvider = "dp")
    public void testSample1(int num) {
        assertNotEquals(num, 0);
    }

    @Test (dataProvider = "dp")
    public void testSample2(int num) {
        assertNotEquals(num, 0);
    }

    @Test
    public void ordinaryTestMethod() {}

    @Test
    public void dontChangeName() {}

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        if (! ("dontChangeName".equals(testResult.getMethod().getMethodName()))) {
            Object param = "999";
            Object[] parameters = callBack.getParameters();
            if (parameters.length != 0) {
                param = parameters[0];
            }
            String testName = name(testResult.getMethod().getMethodName(), param);
            testResult.setTestName(testName);
        }
        callBack.runTestMethod(testResult);
    }

    private static String name(String prefix, Object count) {
        return prefix + "_TestNG_TestCase_" + count.toString();
    }
}
