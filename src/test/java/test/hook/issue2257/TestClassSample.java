package test.hook.issue2257;

import org.testng.Assert;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.ITestResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestClassSample implements IConfigurable {

  private int counter = 1;

  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    callBack.runConfigurationMethod(testResult);
    if (testResult.getThrowable() != null) {
      for (int i = 0; i <= 3; i++) {
        callBack.runConfigurationMethod(testResult);
        if (testResult.getThrowable() == null) {
          break;
        }
      }
    }
  }

  @BeforeSuite
  public void beforeSuite() {
    runConfiguration();
  }

  @BeforeTest
  public void beforeTest() {
    runConfiguration();
  }

  @BeforeClass
  public void beforeClass() {
    runConfiguration();
  }

  @BeforeMethod
  public void beforeMethod() {
    runConfiguration();
  }

  @Test
  public void runTest() {}

  private void runConfiguration() {
    if (counter++ == 2) {
      counter = 1;
    } else {
      Assert.fail();
    }
  }
}
