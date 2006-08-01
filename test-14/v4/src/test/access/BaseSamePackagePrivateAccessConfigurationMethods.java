package test.access;

import test.BasePrivateAccessConfigurationMethods;


public class BaseSamePackagePrivateAccessConfigurationMethods 
extends BasePrivateAccessConfigurationMethods {
  protected boolean m_samePackagePublic = true;
  protected boolean m_samePackageProtected = false;
  protected boolean m_samePackageDefault = false;
  protected boolean m_samePackagePrivate = true;

  /**
   * @testng.configuration beforeTestMethod="true"
   */
  void sameDefaultConfBeforeMethod() {
    m_samePackageDefault = true;
  }
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  protected void sameProtectedConfBeforeMethod() {
    m_samePackageProtected = true;
  }

  /**
   * @testng.configuration beforeTestMethod="true"
   */
  private void samePrivateConfBeforeMethod() {
    m_samePackagePrivate = false;
  }
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void samePublicConfBeforeMethod() {
    m_samePackagePublic = true;
  }
}
