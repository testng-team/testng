package test.hook;

import org.testng.Assert;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.ITestResult;
import org.testng.annotations.Test;


/**
 * Test harness for {@link IConfigurable}
 */
public class ConfigurableSuccessTest extends BaseConfigurable {
  @Override
  public void run(IConfigureCallBack callBack, ITestResult testResult) {
    m_hookCount++;
    callBack.runConfigurationMethod(testResult);
  }

  @Test
  public void hookWasRun() {
    // Note: this value will depend on what other classes are in the same <test> and
    // <suite>, so not a very accurate test
    Assert.assertEquals(m_hookCount, 4);
    Assert.assertTrue(m_bs);
    Assert.assertTrue(m_bt);
    Assert.assertTrue(m_bc);
    Assert.assertTrue(m_bm);
  }
}
