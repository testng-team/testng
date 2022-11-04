package test.hook.samples;

import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CallBackSample {

  public static class ConfigCallBackSkipTestCase implements IConfigurable {

    @Override
    public void run(IConfigureCallBack callBack, ITestResult testResult) {}

    @BeforeMethod
    public void beforeMethod() {}

    @Test
    public void testMethod() {}
  }

  public static class TestCallBackSkipTestCase implements IHookable {
    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {}

    @Test
    public void testMethod() {}
  }
}
