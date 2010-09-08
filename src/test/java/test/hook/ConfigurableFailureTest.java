package test.hook;

import org.testng.Assert;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.ITestResult;
import org.testng.annotations.Test;

/**
 * Test harness for {@link IConfigurable}
 */
public class ConfigurableFailureTest extends BaseConfigurable {

  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    m_hookCount++;
    // Not calling the callback
  }

  @Test
  public void hookWasNotRun() {
    Assert.assertFalse(m_bc);
    Assert.assertFalse(m_bm);
  }
}
