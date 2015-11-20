package test.hook;

import org.testng.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class HookSuccess862Test implements IHookable {

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        for (int i = 0; i < callBack.getParameters().length; i++) {
            Annotation[] annotations = method.getParameterAnnotations()[i];
            for (Annotation annotation : annotations) {
                if (annotation instanceof Named) {
                    Named named = (Named) annotation;
                    callBack.getParameters()[0] = callBack.getParameters()[0] + named.value();
                }
            }
        }
        callBack.runTestMethod(testResult);
    }

    @DataProvider
    public Object[][] dp() {
        return new Object[][]{
                new Object[]{"foo", "test"}
        };
    }

    @Test(dataProvider = "dp")
    public void verify(@Named("bar") String bar, String test) {
        Assert.assertEquals(bar, "foobar");
        Assert.assertEquals(test, "test");
    }
}
