package test;

public class BasePrivateAccessConfigurationMethods {
  protected boolean m_baseProtected = false;
  protected boolean m_baseDefault = true;
  protected boolean m_basePrivate = true;
  protected boolean m_basePublic = true;
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  void baseDefaultConfBeforeMethod() {
    m_baseDefault = false;
  }
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  protected void baseProtectedConfBeforeMethod() {
    m_baseProtected = true;
  }

  /**
   * @testng.configuration beforeTestMethod="true"
   */
  private void basePrivateConfBeforeMethod() {
    m_basePrivate = false;
  }
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void basePublicConfBeforeMethod() {
    m_basePublic = true;
  }
}
