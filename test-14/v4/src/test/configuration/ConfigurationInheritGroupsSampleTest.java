package test.configuration;

import org.testng.Assert;

/**
 * @testng.test groups = "group1"
 */
public class ConfigurationInheritGroupsSampleTest {
  private boolean m_ok = false;

  /**
   * @testng.configuration beforeTestMethod = "true"
   */
  public void setUp() {
    m_ok = true;
  }

  public void test1() { 
    Assert.assertTrue(m_ok);
  }
  
  private void ppp(String s) {
    System.out.println("[ConfigurationInheritGroupsSampleTest] " + s);
  }
}
