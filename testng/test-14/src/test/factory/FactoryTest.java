package test.factory;

import org.testng.Assert;

public class FactoryTest {
  
  static boolean m_invoked = false;
  
  /**
   * @testng.before-suite
   */
  public void init() {
    m_invoked = false;
  }
  
  /**
   * @testng.parameters value = "factory-param"
   * @testng.factory
   */
  public Object[] createObjects(String param) {
    assert "FactoryParam".equals(param) : "Incorrect param: " + param;

    Assert.assertFalse(m_invoked, "Should only be invoked once");
    m_invoked = true;
    
    return new Object[] {
        new FactoryTest2(42),
        new FactoryTest2(43)
    };
  }
  
}