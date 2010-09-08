package test.hook;

import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.Test;

/**
 * Test harness for {@link IHookable}
 * 
 * @author <a href="mailto:cedric@beust.com">Cedric Beust</a>
 * @since Aug 01, 2006
 */
public class HookSuccessTest implements IHookable {
  public static boolean m_hook = false;
  public static boolean m_testWasRun = false;

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    m_hook = true;
    callBack.runTestMethod(testResult);
  }

  @Test
  public void verify() {
    m_testWasRun = true;
    Reporter.log("output from hook test.verify");
  }

//  @AfterMethod
//  public void tearDown() {
//    Assert.assertTrue(m_hook);
//    Assert.assertTrue(m_testWasRun);
//  }

  private void ppp(String string) {
    System.out.println("[HookTest] " + string);
  }
  
}
