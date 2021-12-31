package test.hook.samples;

import static test.hook.HookableTest.*;

import java.util.UUID;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class HookSuccessTimeoutSample implements IHookable {

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    testResult.setAttribute(HOOK_INVOKED_ATTRIBUTE, "true");
    testResult.setAttribute(HOOK_METHOD_PARAMS_ATTRIBUTE, callBack.getParameters());
    callBack.runTestMethod(testResult);
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {new Object[] {UUID.randomUUID()}};
  }

  @Test(dataProvider = "dp", timeOut = 100)
  public void verify(UUID uuid) {
    Reporter.getCurrentTestResult().setAttribute(HOOK_METHOD_INVOKED_ATTRIBUTE, uuid);
  }
}
