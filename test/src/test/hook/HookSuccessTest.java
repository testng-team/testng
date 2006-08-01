package test.hook;

import org.testng.Assert;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class HookSuccessTest implements IHookable {
  private boolean m_hook = false;
  private boolean m_testWasRun = false;

  public void run(IHookCallBack callBack, ITestResult testResult) {
    m_hook = true;
    callBack.runTestMethod(testResult);
  }

  @Test
  public void verify() {
    m_testWasRun = true;
  }
  
  @AfterMethod
  public void tearDown() {
    Assert.assertTrue(m_hook);
    Assert.assertTrue(m_testWasRun);
  }

  private void ppp(String string) {
    System.out.println("[HookTest] " + string);
  }
  
}
