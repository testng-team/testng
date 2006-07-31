package test.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

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

  @Configuration(beforeSuite = true)
  public void beforeSuite() {
    ppp("@@@@ BEFORE_SUITE");
    assert ! m_afterSuite : "afterSuite shouldn't have run";
    assert ! m_beforeClass : "beforeClass shouldn't have run";
    assert ! m_afterClass : "afterClass shouldn't have run";
    assert ! m_beforeMethod: "beforeMethod shouldn't have run";
    assert ! m_afterMethod: "afterMethod shouldn't have run";
    m_beforeSuite = true;
  }
  
  @Configuration(beforeTestClass = true)
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

  @Configuration(beforeTestMethod = true)
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
  
  @Configuration(afterTestMethod = true)
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

  @Configuration(afterTestClass = true)
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

  @Configuration(afterSuite = true)
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
