package test.hook.samples;

import static test.hook.HookableTest.HOOK_INVOKED_ATTRIBUTE;

import org.testng.IConfigureCallBack;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class ConfigurableFailureSample extends BaseConfigurableSample {

  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true");
    // Not calling the callback
  }

  @Test
  public void hookWasNotRun() {}
}
