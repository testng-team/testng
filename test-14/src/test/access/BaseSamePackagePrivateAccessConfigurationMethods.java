package test.access;

import test.BasePrivateAccessConfigurationMethods;


public class BaseSamePackagePrivateAccessConfigurationMethods 
extends BasePrivateAccessConfigurationMethods {
  protected boolean m_samePackagePublic = true;
  protected boolean m_samePackageProtected = false;
  protected boolean m_samePackageDefault = false;
  protected boolean m_samePackagePrivate = true;

  /**
   * @testng.before-method
   */
  void sameDefaultConfBeforeMethod() {
    m_samePackageDefault = true;
  }
  
  /**
   * @testng.before-method
   */
  protected void sameProtectedConfBeforeMethod() {
    m_samePackageProtected = true;
  }

  /**
   * @testng.before-method
   */
  private void samePrivateConfBeforeMethod() {
    m_samePackagePrivate = false;
  }
  
  /**
   * @testng.before-method
   */
  public void samePublicConfBeforeMethod() {
    m_samePackagePublic = true;
  }
}
