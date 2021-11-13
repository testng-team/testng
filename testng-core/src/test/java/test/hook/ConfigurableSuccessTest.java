package test.hook;

import java.lang.reflect.Method;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.ITestResult;
import org.testng.annotations.Test;

/** Test harness for {@link IConfigurable} */
public class ConfigurableSuccessTest extends BaseConfigurable {
  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    m_hookCount++;
    Object[] parameters = callBack.getParameters();
    if (parameters.length > 0) {
      m_methodName = ((Method) parameters[0]).getName();
    }
    callBack.runConfigurationMethod(testResult);
  }

  @Test
  public void hookWasRun() {}
}
