package test.hook;

import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.ITestResult;

import java.lang.reflect.Method;

public class ConfigurableListener implements IConfigurable {
  static int m_hookCount = 0;
  static String m_methodName;

  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    m_hookCount++;
    Object[] parameters = callBack.getParameters();
    if (parameters.length > 0) {
      m_methodName = ((Method) parameters[0]).getName();
    }
    callBack.runConfigurationMethod(testResult);
  }

}
