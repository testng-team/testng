package test.access;

import org.testng.Assert;

import test.BasePrivateAccessConfigurationMethods;


public class PrivateAccessConfigurationMethods 
extends BaseSamePackagePrivateAccessConfigurationMethods {
  private boolean m_private = false;
  private boolean m_default = false;
  private boolean m_protected = false;
  private boolean m_public = false;
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  private void privateConfBeforeMethod() {
    m_private = true;
  }

  /**
   * @testng.configuration beforeTestMethod="true"
   */
  void defaultConfBeforeMethod() {
    m_default = true;
  }
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  protected void protectedConfBeforeMethod() {
    m_protected = true;
  }

  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void publicConfBeforeMethod() {
    m_public = true;
  }
  

  
  /**
   * @testng.test
   */
  public void allAccessModifiersConfiguration() {
    Assert.assertTrue(m_private, "private @Configuration should have been run");
    Assert.assertTrue(m_default, "default @Configuration should have been run");
    Assert.assertTrue(m_protected, "protected @Configuration should have been run");
    Assert.assertTrue(m_public, "public @Configuration should have been run");
    
    Assert.assertTrue(m_samePackagePublic, "public base @Configuration should have been run");
    Assert.assertTrue(m_samePackageProtected, "protected base @Configuration should have been run"); 
    Assert.assertTrue(m_samePackageDefault, "default base @Configuration should have been run"); 
    Assert.assertTrue(m_samePackagePrivate, "private base @Configuration should not have been run"); 
    
    Assert.assertTrue(m_basePublic, "public external base @Configuration should have been run");
    Assert.assertTrue(m_baseProtected, "protected external base @Configuration should have been run"); 
    Assert.assertTrue(m_baseDefault, "default external base @Configuration should not have been run"); 
    Assert.assertTrue(m_basePrivate, "private external base @Configuration should not have been run");     
  }
}
