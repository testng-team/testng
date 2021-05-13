package test.hook;

import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class HookSuccessTest implements IHookable {
  static boolean m_hook = false;
  static boolean m_testWasRun = false;
  static String m_parameter = null;

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    m_hook = true;
    Object[] parameters = callBack.getParameters();
    if (parameters.length > 0) {
      m_parameter = parameters[0].toString();
    }
    callBack.runTestMethod(testResult);
  }

  @DataProvider
  public Object[][] dp() {
    return new Object[][] {new Object[] {"foo"}};
  }

  @Test(dataProvider = "dp")
  public void verify(String name) {
    m_testWasRun = true;
    Reporter.log("output from hook test.verify");
  }
}
