package test.hook.samples;

import static test.hook.HookableTest.HOOK_INVOKED_ATTRIBUTE;

import org.testng.IConfigureCallBack;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class ConfigurableFailureWithStatusAlteredSample extends BaseConfigurableSample {

  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true");
    // Not calling the callback
    testResult.setStatus(ITestResult.SUCCESS);
  }

  @Test
  public void hookWasNotRun() {}
}
