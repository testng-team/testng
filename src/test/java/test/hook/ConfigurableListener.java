package test.hook;

import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.ITestResult;

public class ConfigurableListener implements IConfigurable {
  public static int m_hookCount = 0;

  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    m_hookCount++;
    callBack.runConfigurationMethod(testResult);
  }

}
