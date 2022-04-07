package test.hook.samples;

import static test.hook.HookableTest.HOOK_INVOKED_ATTRIBUTE;
import static test.hook.HookableTest.HOOK_METHOD_INVOKED_ATTRIBUTE;
import static test.hook.HookableTest.HOOK_METHOD_PARAMS_ATTRIBUTE;

import java.util.UUID;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class HookSuccessTimeoutSample implements IHookable {

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true");
    testResult.setAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE, callBack.getParameters());
    callBack.runTestMethod(testResult);
  }

  @Test(timeOut = 100)
  public void verify() {
    Reporter.getCurrentTestResult().setAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE, UUID.randomUUID());
  }
}
