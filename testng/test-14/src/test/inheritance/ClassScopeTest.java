package test.inheritance;

import org.testng.Assert;

public class ClassScopeTest extends BaseClassScope {

  private boolean m_verify = false;

  public void setVerify() {
    m_verify = true;
  }
  
  /**
   * @testng.test dependsOnMethods = "setVerify"
   */
  public void verify() {
    Assert.assertTrue(m_verify);
  }
}
