package test.hook.samples;

import static test.hook.HookableTest.HOOK_INVOKED_ATTRIBUTE;
import static test.hook.HookableTest.HOOK_METHOD_PARAMS_ATTRIBUTE;

import org.testng.IConfigureCallBack;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class ConfigurableSuccessSample extends BaseConfigurableSample {
  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true");
    testResult.setAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE, callBack.getParameters());
    callBack.runConfigurationMethod(testResult);
  }

  @Test
  public void hookWasRun() {}
}
