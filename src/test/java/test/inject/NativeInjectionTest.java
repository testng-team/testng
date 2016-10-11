package test.inject;

import org.testng.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import test.SimpleBaseTest;

import static test.inject.NativeInjectionTestSamples.*;

public class NativeInjectionTest extends SimpleBaseTest {

    @Test (dataProvider = "getTestData")
    public void testBeforeSuiteInjection(Class clazz, String methodName) {
        TestNG tng = create(clazz);
        InjectionResultHolder holder = new InjectionResultHolder();
        tng.addListener((ITestNGListener) holder);
        tng.setGroups("test");
        tng.run();
        String actual = "Cannot inject @Configuration annotated Method [" + methodName + "] with [int].";
        Assert.assertTrue(holder.getErrorMessage().contains(actual));
    }

    @DataProvider
    public Object[][] getTestData() {
        return new Object[][] {
            {BadBeforeSuiteSample.class, "beforeSuite"},
            {BadBeforeTestSample.class, "beforeTest"},
            {BadBeforeClassSample.class, "beforeClass"},
            {BadBeforeMethodSample.class, "beforeMethod"},
            {BadAfterMethodSample.class, "afterMethod"},
            {BadAfterClassSample.class, "afterClass"},
            {BadAfterTestSample.class, "afterTest"},
            {BadAfterSuiteSample.class, "afterSuite"},
            {BadBeforeGroupsSample.class, "beforeGroups"},
            {BadAfterGroupsSample.class, "afterGroups"}
        };
    }

    public static class InjectionResultHolder extends TestListenerAdapter {
        private String errorMessage;

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public void onConfigurationFailure(ITestResult itr) {
            this.errorMessage = itr.getThrowable().getMessage();
        }

    }
}
