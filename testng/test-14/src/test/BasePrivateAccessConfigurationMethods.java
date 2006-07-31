package test;

public class BasePrivateAccessConfigurationMethods {
  protected boolean m_baseProtected = false;
  protected boolean m_baseDefault = true;
  protected boolean m_basePrivate = true;
  protected boolean m_basePublic = true;
  
  /**
   * @testng.before-method
   */
  void baseDefaultConfBeforeMethod() {
    m_baseDefault = false;
  }
  
  /**
   * @testng.before-method
   */
  protected void baseProtectedConfBeforeMethod() {
    m_baseProtected = true;
  }

  /**
   * @testng.before-method
   */
  private void basePrivateConfBeforeMethod() {
    m_basePrivate = false;
  }
  
  /**
   * @testng.before-method
   */
  public void basePublicConfBeforeMethod() {
    m_basePublic = true;
  }
}
