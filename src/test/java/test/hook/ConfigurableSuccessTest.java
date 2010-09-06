package test.hook;

import org.testng.Assert;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.ITestResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/**
 * Test harness for {@link IConfigurable}
 */
public class ConfigurableSuccessTest implements IConfigurable {
  private boolean m_hook = false;
  private boolean m_wasRun = false;

  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    m_hook = true;
    callBack.runConfigurationMethod(testResult);
  }

  @BeforeMethod
  public void bm() {
    m_wasRun = true;
  }

  @Test
  public void hookWasRun() {
    Assert.assertTrue(m_hook);
    Assert.assertTrue(m_wasRun);
  }
}
