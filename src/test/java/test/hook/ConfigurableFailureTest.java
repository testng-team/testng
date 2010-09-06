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
public class ConfigurableFailureTest implements IConfigurable {
  private boolean m_hook = false;
  private boolean m_wasRun = false;

  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    m_hook = true;
    // Not calling the callback
  }

  @BeforeMethod
  public void bm() {
    m_wasRun = true;
  }

  @Test
  public void hookWasRun() {
    Assert.assertTrue(m_hook);
    Assert.assertFalse(m_wasRun);
  }
}
