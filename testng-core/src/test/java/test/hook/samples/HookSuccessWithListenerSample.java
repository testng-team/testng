package test.hook.samples;

import static test.hook.HookableTest.*;

import org.testng.Assert;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(HookSuccessWithListenerSample.HookListener.class)
public class HookSuccessWithListenerSample {

  @Test
  public void verify() {
    ITestResult itr = Reporter.getCurrentTestResult();
    boolean attribute = Boolean.parseBoolean(itr.getAttribute(HOOK_INVOKED_ATTRIBUTE).toString());
    Assert.assertTrue(attribute);
  }

  public static class HookListener implements IHookable {

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
      testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true");
      callBack.runTestMethod(testResult);
    }
  }
}
