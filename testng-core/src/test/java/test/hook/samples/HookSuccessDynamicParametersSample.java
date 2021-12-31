package test.hook.samples;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.inject.Named;
import org.testng.Assert;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class HookSuccessDynamicParametersSample implements IHookable {

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
    return new Object[][] {new Object[] {"foo", "test"}};
  }

  @Test(dataProvider = "dp")
  public void verify(@Named("bar") String bar, String test) {
    Assert.assertEquals(bar, "foobar");
    Assert.assertEquals(test, "test");
  }
}
