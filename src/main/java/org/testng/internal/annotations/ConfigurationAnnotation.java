package org.testng.internal.annotations;

import org.testng.annotations.IConfigurationAnnotation;


/**
 * An implementation of IConfiguration
 *
 * Created on Dec 16, 2005
 * @author cbeust
 */
public class ConfigurationAnnotation extends TestOrConfiguration implements IConfigurationAnnotation,
    IBeforeSuite, IAfterSuite,
    IBeforeTest, IAfterTest,
    IBeforeGroups, IAfterGroups,
    IBeforeClass, IAfterClass,
    IBeforeMethod, IAfterMethod {
  private boolean m_beforeTestClass = false;
  private boolean m_afterTestClass = false;
  private boolean m_beforeTestMethod = false;
  private boolean m_afterTestMethod = false;
  private boolean m_beforeTest = false;
  private boolean m_afterTest = false;
  private boolean m_beforeSuite = false;
  private boolean m_afterSuite = false;
  private String[] m_parameters = {};
  private boolean m_alwaysRun = false;
  private boolean m_inheritGroups = true;
  private String[] m_beforeGroups = {};
  private String[] m_afterGroups = {};
  private boolean m_isFakeConfiguration;
  private boolean m_firstTimeOnly = false;
  private boolean m_lastTimeOnly = false;

  public ConfigurationAnnotation() {

  }

  public void setAfterSuite(boolean afterSuite) {
    m_afterSuite = afterSuite;
  }

  public void setAfterTest(boolean afterTest) {
    m_afterTest = afterTest;
  }

  public void setAfterTestClass(boolean afterTestClass) {
    m_afterTestClass = afterTestClass;
  }

  public void setAfterTestMethod(boolean afterTestMethod) {
    m_afterTestMethod = afterTestMethod;
  }

  public void setAlwaysRun(boolean alwaysRun) {
    m_alwaysRun = alwaysRun;
  }

  public void setBeforeSuite(boolean beforeSuite) {
    m_beforeSuite = beforeSuite;
  }

  public void setBeforeTest(boolean beforeTest) {
    m_beforeTest = beforeTest;
  }

  public void setBeforeTestClass(boolean beforeTestClass) {
    m_beforeTestClass = beforeTestClass;
  }

  public void setBeforeTestMethod(boolean beforeTestMethod) {
    m_beforeTestMethod = beforeTestMethod;
  }

  public void setInheritGroups(boolean inheritGroups) {
    m_inheritGroups = inheritGroups;
  }

  @Override
  public void setParameters(String[] parameters) {
    m_parameters = parameters;
  }

  @Override
  public boolean getBeforeTestClass() {
    return m_beforeTestClass;
  }

  @Override
  public boolean getAfterTestClass() {
    return m_afterTestClass;
  }

  @Override
  public boolean getBeforeTestMethod() {
    return m_beforeTestMethod;
  }

  @Override
  public boolean getAfterTestMethod() {
    return m_afterTestMethod;
  }

  @Override
  public boolean getBeforeSuite() {
    return m_beforeSuite;
  }

  @Override
  public boolean getAfterSuite() {
    return m_afterSuite;
  }

  @Override
  public boolean getBeforeTest() {
    return m_beforeTest;
  }

  @Override
  public boolean getAfterTest() {
    return m_afterTest;
  }

  @Override
  public String[] getParameters() {
    return m_parameters;
  }

  @Override
  public boolean getAlwaysRun() {
    return m_alwaysRun;
  }

  @Override
  public boolean getInheritGroups() {
    return m_inheritGroups;
  }

  @Override
  public String[] getAfterGroups() {
    return m_afterGroups;
  }

  public void setAfterGroups(String[] afterGroups) {
    m_afterGroups = afterGroups;
  }

  @Override
  public String[] getBeforeGroups() {
    return m_beforeGroups;
  }

  public void setBeforeGroups(String[] beforeGroups) {
    m_beforeGroups = beforeGroups;
  }

  public void setFakeConfiguration(boolean b) {
    m_isFakeConfiguration = b;
  }

  @Override
  public boolean isFakeConfiguration() {
    return m_isFakeConfiguration;
  }

  public void setFirstTimeOnly(boolean f) {
    m_firstTimeOnly = f;
  }

  public boolean isFirstTimeOnly() {
    return m_firstTimeOnly;
  }

  public void setLastTimeOnly(boolean f) {
    m_lastTimeOnly = f;
  }

  public boolean isLastTimeOnly() {
    return m_lastTimeOnly;
  }
}
