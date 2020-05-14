package test.inheritance;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ClassScopeTest extends BaseClassScope {

  private boolean m_verify = false;

  public void setVerify() {
    m_verify = true;
  }

  @Test(dependsOnMethods = "setVerify")
  public void verify() {
    Assert.assertTrue(m_verify);
  }
}
