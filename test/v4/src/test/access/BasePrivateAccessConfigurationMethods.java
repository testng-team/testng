package test.access;

import org.testng.annotations.Configuration;

public class BasePrivateAccessConfigurationMethods {
  protected boolean m_baseProtected = false;
  protected boolean m_baseDefault = false;
  protected boolean m_basePrivate = true;
  
  @Configuration(beforeTestMethod = true)
  void baseDefaultConfBeforeMethod() {
    m_baseDefault = true;
  }
  
  @Configuration(beforeTestMethod = true)
  protected void baseProtectedConfBeforeMethod() {
    m_baseProtected = true;
  }

  @Configuration(beforeTestMethod = true)
  private void basePrivateConfBeforeMethod() {
    m_basePrivate = false;
  }
}
