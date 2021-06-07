package test.hook;

import java.lang.reflect.Method;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.ITestResult;

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
