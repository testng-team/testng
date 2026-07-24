package test.access;

import org.testng.annotations.BeforeMethod;

public class BasePrivateAccessConfigurationMethods {
  protected boolean m_baseProtected;
  protected boolean m_baseDefault;
  protected boolean m_basePrivate = true;

  @BeforeMethod
  void baseDefaultConfBeforeMethod() {
    m_baseDefault = true;
  }

  @BeforeMethod
  protected void baseProtectedConfBeforeMethod() {
    m_baseProtected = true;
  }

  @BeforeMethod
  private void basePrivateConfBeforeMethod() {
    m_basePrivate = false;
  }
}
