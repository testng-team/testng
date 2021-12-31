package test.hook.samples;

import static test.hook.HookableTest.HOOK_INVOKED_ATTRIBUTE;
import static test.hook.HookableTest.HOOK_METHOD_INVOKED_ATTRIBUTE;

import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class HookFailureSample implements IHookable {

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true");
    // Not invoking the callback:  the method should not be run
  }

  @Test
  public void verify() {
    Reporter.getCurrentTestResult().setAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE, "true");
  }
}
