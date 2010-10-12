package test.hook;

import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class HookFailureTest implements IHookable {
  static boolean m_hook = false;
  static boolean m_testWasRun = false;

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    m_hook = true;
    // Not invoking the callback:  the method should not be run
  }

  @Test
  public void verify() {
    m_testWasRun = true;
  }

//  @AfterMethod
//  public void tearDown() {
//    Assert.assertTrue(m_hook);
//    Assert.assertFalse(m_testWasRun);
//  }

  private void ppp(String string) {
    System.out.println("[HookFailureTest] " + string);
  }


}
