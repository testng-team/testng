package test.hook;

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
//    Assert.assertEquals(m_hookCount, 2);
//    Assert.assertTrue(m_bc);
//    Assert.assertTrue(m_bm);
  }
}
