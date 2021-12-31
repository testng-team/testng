package test.hook.samples;

import static test.hook.HookableTest.HOOK_INVOKED_ATTRIBUTE;
import static test.hook.HookableTest.HOOK_METHOD_PARAMS_ATTRIBUTE;

import java.lang.reflect.Method;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.ITestResult;
import org.testng.annotations.*;

@Listeners(ConfigurableSuccessWithListenerSample.ConfigurableListener.class)
public class ConfigurableSuccessWithListenerSample {

  @BeforeSuite
  public void bs() {}

  @BeforeMethod
  public void bt() {}

  @BeforeMethod
  public void bm(Method m) {}

  @BeforeClass
  public void bc() {}

  @Test
  public void hookWasRun() {}

  public static class ConfigurableListener implements IConfigurable {

    @Override
    public void run(IConfigureCallBack callBack, ITestResult testResult) {
      testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true");
      testResult.setAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE, callBack.getParameters());
      callBack.runConfigurationMethod(testResult);
    }
  }
}
