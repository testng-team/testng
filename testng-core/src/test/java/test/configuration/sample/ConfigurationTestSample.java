package test.configuration.sample;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class ConfigurationTestSample {
  private boolean m_beforeSuite = false;
  private boolean m_afterSuite = false;
  private boolean m_beforeClass = false;
  private boolean m_afterClass = false;
  private boolean m_beforeMethod = false;
  private boolean m_afterMethod = false;

  @BeforeSuite
  public void beforeSuite() {
    assertFalse(m_afterSuite, "afterSuite shouldn't have run");
    assertFalse(m_beforeClass, "beforeClass shouldn't have run");
    assertFalse(m_afterClass, "afterClass shouldn't have run");
    assertFalse(m_beforeMethod, "beforeMethod shouldn't have run");
    assertFalse(m_afterMethod, "afterMethod shouldn't have run");
    m_beforeSuite = true;
  }

  @BeforeClass
  public void beforeClass() {
    assertTrue(m_beforeSuite, "beforeSuite should have run");
    assertFalse(m_afterSuite, "afterSuite shouldn't have run");
    assertFalse(m_beforeClass, "beforeClass shouldn't have run");
    assertFalse(m_afterClass, "afterClass shouldn't have run");
    assertFalse(m_beforeMethod, "beforeMethod shouldn't have run");
    assertFalse(m_afterMethod, "afterMethod shouldn't have run");
    m_beforeClass = true;
  }

  @BeforeMethod
  public void beforeMethod() {
    assertTrue(m_beforeSuite, "beforeSuite should have run");
    assertTrue(m_beforeClass, "beforeClass have run");
    assertFalse(m_afterSuite, "afterSuite shouldn't have run");
    assertFalse(m_afterClass, "afterClass shouldn't have run");
    assertFalse(m_beforeMethod, "beforeMethod shouldn't have run");
    assertFalse(m_afterMethod, "afterMethod shouldn't have run");
    m_beforeMethod = true;
  }

  @AfterMethod
  public void afterMethod() {
    assertTrue(m_beforeSuite, "beforeSuite should have run");
    assertTrue(m_beforeClass, "beforeClass have run");
    assertTrue(m_beforeMethod, "beforeMethod should have run");
    assertFalse(m_afterSuite, "afterSuite shouldn't have run");
    assertFalse(m_afterClass, "afterClass shouldn't have run");
    assertFalse(m_afterMethod, "afterMethod shouldn't have run");
    m_afterMethod = true;
  }

  @AfterClass
  public void afterClass() {
    assertTrue(m_beforeSuite, "beforeSuite should have run");
    assertTrue(m_beforeClass, "beforeClass have run");
    assertTrue(m_beforeMethod, "beforeMethod should have run");
    assertTrue(m_afterMethod, "afterMethod should have run");
    assertFalse(m_afterClass, "afterClass shouldn't have run");
    assertFalse(m_afterSuite, "afterSuite shouldn't have run");
    m_afterClass = true;
  }

  @AfterSuite
  public void afterSuite() {
    assertTrue(m_beforeSuite, "beforeSuite should have run");
    assertTrue(m_beforeClass, "beforeClass have run");
    assertTrue(m_beforeMethod, "beforeMethod should have run");
    assertTrue(m_afterMethod, "afterMethod should have run");
    assertTrue(m_afterClass, "afterClass should have run");
    assertFalse(m_afterSuite, "afterSuite shouldn't have run");
    m_afterSuite = true;
  }

  @Test
  public void verify() {
    assertTrue(m_beforeSuite, "beforeSuite should have run");
    assertTrue(m_beforeClass, "beforeClass have run");
    assertTrue(m_beforeMethod, "beforeMethod should have run");
    assertFalse(m_afterSuite, "afterSuite shouldn't have run");
    assertFalse(m_afterClass, "afterClass shouldn't have run");
    assertFalse(m_afterMethod, "afterMethod shouldn't have run");
  }
}
