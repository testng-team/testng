package test.configuration;

/**
 * Test @Configuration
 * 
 * @author cbeust
 */
public class ConfigurationTest {
  private boolean m_beforeSuite = false;
  private boolean m_afterSuite = false;
  private boolean m_beforeClass = false;
  private boolean m_afterClass = false;
  private boolean m_beforeMethod  = false;
  private boolean m_afterMethod = false;

  /**
   * @testng.configuration beforeSuite = "true"
   */
  public void beforeSuite() {
    ppp("@@@@ BEFORE_SUITE");
    assert ! m_afterSuite : "afterSuite shouldn't have run";
    assert ! m_beforeClass : "beforeClass shouldn't have run";
    assert ! m_afterClass : "afterClass shouldn't have run";
    assert ! m_beforeMethod: "beforeMethod shouldn't have run";
    assert ! m_afterMethod: "afterMethod shouldn't have run";
    m_beforeSuite = true;
  }
  
  /**
   * @testng.configuration beforeTestClass = "true"
   */
  public void beforeClass() {
    ppp("@@@@ BEFORE_CLASS");
    assert m_beforeSuite : "beforeSuite should have run";
    assert ! m_afterSuite : "afterSuite shouldn't have run";
    assert ! m_beforeClass : "beforeClass shouldn't have run";
    assert ! m_afterClass : "afterClass shouldn't have run";
    assert ! m_beforeMethod: "beforeMethod shouldn't have run";
    assert ! m_afterMethod: "afterMethod shouldn't have run";
    m_beforeClass = true;
  }

  /**
   * @testng.configuration beforeTestMethod = "true"
   */
  public void beforeMethod() {
    ppp("@@@@ BEFORE_METHOD");
    assert m_beforeSuite : "beforeSuite should have run";
    assert m_beforeClass : "beforeClass have run";
    assert ! m_afterSuite : "afterSuite shouldn't have run";
    assert ! m_afterClass : "afterClass shouldn't have run";
    assert ! m_beforeMethod: "beforeMethod shouldn't have run";
    assert ! m_afterMethod: "afterMethod shouldn't have run";
    m_beforeMethod = true;
  }
  
  /**
   * @testng.configuration afterTestMethod = "true"
   */
  public void afterMethod() {
    ppp("@@@@ AFTER_METHOD");
    assert m_beforeSuite : "beforeSuite should have run";
    assert m_beforeClass : "beforeClass have run";
    assert m_beforeMethod: "beforeMethod should have run";
    assert ! m_afterSuite : "afterSuite shouldn't have run";
    assert ! m_afterClass : "afterClass shouldn't have run";
    assert ! m_afterMethod: "afterMethod shouldn't have run";
    m_afterMethod = true;
  }

  /**
   * @testng.configuration afterTestClass = "true"
   */
  public void afterClass() {
    ppp("@@@@ AFTER_CLASS");
    assert m_beforeSuite : "beforeSuite should have run";
    assert m_beforeClass : "beforeClass have run";
    assert m_beforeMethod: "beforeMethod should have run";
    assert m_afterMethod: "afterMethod should have run";
    assert ! m_afterClass : "afterClass shouldn't have run";
    assert ! m_afterSuite : "afterSuite shouldn't have run";
    m_afterClass = true;
  }

  /**
   * @testng.configuration afterSuite = "true"
   */
  public void afterSuite() {
    ppp("@@@@ AFTER_SUITE");
    assert m_beforeSuite : "beforeSuite should have run";
    assert m_beforeClass : "beforeClass have run";
    assert m_beforeMethod: "beforeMethod should have run";
    assert m_afterMethod: "afterMethod should have run";
    assert m_afterClass : "afterClass should have run";
    assert ! m_afterSuite : "afterSuite shouldn't have run";
    m_afterSuite = true;
  }

  /**
   * @testng.test
   */
  public void verify() {
    ppp("@@@@ VERIFY");
    assert m_beforeSuite : "beforeSuite should have run";
    assert m_beforeClass : "beforeClass have run";
    assert m_beforeMethod: "beforeMethod should have run";
    assert ! m_afterSuite : "afterSuite shouldn't have run";
    assert ! m_afterClass : "afterClass shouldn't have run";
    assert ! m_afterMethod: "afterMethod shouldn't have run";
  }
  
  private static void ppp(String s) {
    if (false) {
      System.out.println("[ConfigurationTest] " + s);
    }
  }
  
}
