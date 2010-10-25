package test.configuration;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

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

  @BeforeSuite
  public void beforeSuite() {
    ppp("@@@@ BEFORE_SUITE");
    assert ! m_afterSuite : "afterSuite shouldn't have run";
    assert ! m_beforeClass : "beforeClass shouldn't have run";
    assert ! m_afterClass : "afterClass shouldn't have run";
    assert ! m_beforeMethod: "beforeMethod shouldn't have run";
    assert ! m_afterMethod: "afterMethod shouldn't have run";
    m_beforeSuite = true;
  }

  @BeforeClass
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

  @BeforeMethod
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

  @AfterMethod
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

  @AfterClass
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

  @AfterSuite
  public void afterSuite() {
    ppp("@@@@ AFTER_SUITE");
    ppp(m_beforeSuite + " " + m_beforeClass + " " + m_beforeMethod
        + " " + m_afterMethod + " " + m_afterClass + " " + m_afterSuite);
    assert m_beforeSuite : "beforeSuite should have run";
    assert m_beforeClass : "beforeClass have run";
    assert m_beforeMethod: "beforeMethod should have run";
    assert m_afterMethod: "afterMethod should have run";
    assert m_afterClass : "afterClass should have run";
    assert ! m_afterSuite : "afterSuite shouldn't have run";
    m_afterSuite = true;
  }

  @Test
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
