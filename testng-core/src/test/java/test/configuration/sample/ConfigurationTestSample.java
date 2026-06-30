package test.configuration.sample;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(m_afterSuite).withFailMessage("afterSuite shouldn't have run").isFalse();
    assertThat(m_beforeClass).withFailMessage("beforeClass shouldn't have run").isFalse();
    assertThat(m_afterClass).withFailMessage("afterClass shouldn't have run").isFalse();
    assertThat(m_beforeMethod).withFailMessage("beforeMethod shouldn't have run").isFalse();
    assertThat(m_afterMethod).withFailMessage("afterMethod shouldn't have run").isFalse();
    m_beforeSuite = true;
  }

  @BeforeClass
  public void beforeClass() {
    assertThat(m_beforeSuite).withFailMessage("beforeSuite should have run").isTrue();
    assertThat(m_afterSuite).withFailMessage("afterSuite shouldn't have run").isFalse();
    assertThat(m_beforeClass).withFailMessage("beforeClass shouldn't have run").isFalse();
    assertThat(m_afterClass).withFailMessage("afterClass shouldn't have run").isFalse();
    assertThat(m_beforeMethod).withFailMessage("beforeMethod shouldn't have run").isFalse();
    assertThat(m_afterMethod).withFailMessage("afterMethod shouldn't have run").isFalse();
    m_beforeClass = true;
  }

  @BeforeMethod
  public void beforeMethod() {
    assertThat(m_beforeSuite).withFailMessage("beforeSuite should have run").isTrue();
    assertThat(m_beforeClass).withFailMessage("beforeClass have run").isTrue();
    assertThat(m_afterSuite).withFailMessage("afterSuite shouldn't have run").isFalse();
    assertThat(m_afterClass).withFailMessage("afterClass shouldn't have run").isFalse();
    assertThat(m_beforeMethod).withFailMessage("beforeMethod shouldn't have run").isFalse();
    assertThat(m_afterMethod).withFailMessage("afterMethod shouldn't have run").isFalse();
    m_beforeMethod = true;
  }

  @AfterMethod
  public void afterMethod() {
    assertThat(m_beforeSuite).withFailMessage("beforeSuite should have run").isTrue();
    assertThat(m_beforeClass).withFailMessage("beforeClass have run").isTrue();
    assertThat(m_beforeMethod).withFailMessage("beforeMethod should have run").isTrue();
    assertThat(m_afterSuite).withFailMessage("afterSuite shouldn't have run").isFalse();
    assertThat(m_afterClass).withFailMessage("afterClass shouldn't have run").isFalse();
    assertThat(m_afterMethod).withFailMessage("afterMethod shouldn't have run").isFalse();
    m_afterMethod = true;
  }

  @AfterClass
  public void afterClass() {
    assertThat(m_beforeSuite).withFailMessage("beforeSuite should have run").isTrue();
    assertThat(m_beforeClass).withFailMessage("beforeClass have run").isTrue();
    assertThat(m_beforeMethod).withFailMessage("beforeMethod should have run").isTrue();
    assertThat(m_afterMethod).withFailMessage("afterMethod should have run").isTrue();
    assertThat(m_afterClass).withFailMessage("afterClass shouldn't have run").isFalse();
    assertThat(m_afterSuite).withFailMessage("afterSuite shouldn't have run").isFalse();
    m_afterClass = true;
  }

  @AfterSuite
  public void afterSuite() {
    assertThat(m_beforeSuite).withFailMessage("beforeSuite should have run").isTrue();
    assertThat(m_beforeClass).withFailMessage("beforeClass have run").isTrue();
    assertThat(m_beforeMethod).withFailMessage("beforeMethod should have run").isTrue();
    assertThat(m_afterMethod).withFailMessage("afterMethod should have run").isTrue();
    assertThat(m_afterClass).withFailMessage("afterClass should have run").isTrue();
    assertThat(m_afterSuite).withFailMessage("afterSuite shouldn't have run").isFalse();
    m_afterSuite = true;
  }

  @Test
  public void verify() {
    assertThat(m_beforeSuite).withFailMessage("beforeSuite should have run").isTrue();
    assertThat(m_beforeClass).withFailMessage("beforeClass have run").isTrue();
    assertThat(m_beforeMethod).withFailMessage("beforeMethod should have run").isTrue();
    assertThat(m_afterSuite).withFailMessage("afterSuite shouldn't have run").isFalse();
    assertThat(m_afterClass).withFailMessage("afterClass shouldn't have run").isFalse();
    assertThat(m_afterMethod).withFailMessage("afterMethod shouldn't have run").isFalse();
  }
}
