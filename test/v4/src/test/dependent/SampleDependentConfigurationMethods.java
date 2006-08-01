package test.dependent;

import org.testng.annotations.*;

public class SampleDependentConfigurationMethods {
  private boolean m_create = false;
  private boolean m_first = false;

  @Configuration(beforeTestMethod = true)
  public void createInstance() {
    m_create = true;
  }
  
  @Configuration(beforeTestMethod = true, dependsOnMethods = { "createInstance"})
  public void firstInvocation() {
    assert m_create : "createInstance() was never called"; 
    m_first = true;
  }
  
  @Test
  public void verifyDependents() {
    assert m_create : "createInstance() was never called";
    assert m_first : "firstInvocation() was never called";
  }
}
