package test.hook;

import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;

public class HookListener implements IHookable {
  public static boolean m_hook = false;

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    m_hook = true;
    callBack.runTestMethod(testResult);
  }

}
