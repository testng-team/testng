package test.dependent;


public class SampleDependentConfigurationMethods {
  private boolean m_create = false;
  private boolean m_first = false;

  /**
   * @testng.configuration beforeTestMethod="true" dependsOnMethods="firstInvocation"
   */
  public void createInstance() {
    assert m_first : "createInstance() was never called"; 
    m_create = true;
  }
  
  /**
   * @testng.configuration beforeTestMethod="true"
   */
  public void firstInvocation() {
    m_first = true;
  }
  
  /**
   * @testng.test
   */
  public void verifyDependents() {
    assert m_create : "createInstance() was never called";
    assert m_first : "firstInvocation() was never called";
  }
}
