package test.tesng1046;

import org.testng.IAlterTestName;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class TestClassSample implements IHookable {

    @DataProvider (name = "dp", parallel = true)
    public Object[][] getTestData() {
        return new Object[][] {
            {1}, {2}, {3}, {4}, {5}
        };
    }

    @Test (dataProvider = "dp")
    public void testSample1(int num) {
        assertTrue(num != 0);
    }

    @Test (dataProvider = "dp")
    public void testSample2(int num) {
        assertTrue(num != 0);
    }

    @Test
    public void ordinaryTestMethod() {

    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        Object[] parameters = callBack.getParameters();
        String testName = name(testResult.getMethod().getMethodName(), "999");
        if (parameters != null && parameters.length != 0) {
            testName = name(testResult.getMethod().getMethodName(), parameters[0]);
        }
        if (testResult instanceof IAlterTestName) {
            ((IAlterTestName) testResult).setTestName(testName);
        }
        callBack.runTestMethod(testResult);
    }

    private String name(String prefix, Object count) {
        return prefix + "_TestNG_TestCase_" + count.toString();
    }
}
