package test.hook.issue2266;

import org.testng.Assert;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public class TestClassSample implements IHookable {

  private int counter = 1;

  @Override
  public void run(IHookCallBack callBack, ITestResult testResult) {
    callBack.runTestMethod(testResult);
    if (testResult.getThrowable() != null) {
      for (int i = 0; i <= 3; i++) {
        callBack.runTestMethod(testResult);
        if (testResult.getThrowable() == null) {
          break;
        }
      }
    }
  }

  @Test(description = "GITHUB-2266")
  public void runTest() {
    if (counter++ != 2) {
      Assert.fail();
    }
  }
}
