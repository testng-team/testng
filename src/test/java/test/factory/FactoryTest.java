package test.factory;

import static org.testng.Assert.assertFalse;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.Factory;
import org.testng.annotations.Parameters;

public class FactoryTest {
  static boolean m_invoked = false;
  
  @Parameters({ "factory-param" })
  @Factory
  public Object[] createObjects(String param) 
  {
    assert "FactoryParam".equals(param) : "Incorrect param: " + param;
    
    assertFalse(m_invoked, "Should only be invoked once");
    m_invoked = true;
    
    return new Object[] {
        new FactoryTest2(42),
        new FactoryTest2(43)
    };
  }
  
  private static void ppp(String s) {
    System.out.println("[FactoryTest] " + s);
  }
  
  @AfterSuite
  public void afterSuite() {
    m_invoked = false;
  }
}