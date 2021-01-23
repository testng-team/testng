package org.testng.internal.annotations;

import org.testng.annotations.IConfigurationAnnotation;

/**
 * An implementation of IConfiguration
 */
public class ConfigurationAnnotation extends TestOrConfiguration
    implements IConfigurationAnnotation,
    IBeforeSuite,
    IAfterSuite,
    IBeforeTest,
    IAfterTest,
    IBeforeGroups,
    IAfterGroups,
    IBeforeClass,
    IAfterClass,
    IBeforeMethod,
    IAfterMethod {

  private boolean m_beforeTestClass = false;
  private boolean m_afterTestClass = false;
  private boolean m_beforeTestMethod = false;
  private boolean m_afterTestMethod = false;
  private boolean m_beforeTest = false;
  private boolean m_afterTest = false;
  private boolean m_beforeSuite = false;
  private boolean m_afterSuite = false;
  private boolean m_alwaysRun = false;
  private boolean m_inheritGroups = true;
  private boolean m_isBeforeGroups = false;
  private boolean m_isAfterGroups = false;
  private String[] m_beforeGroups = {};
  private String[] m_afterGroups = {};
  private String[] m_groupFilters = {};
  private boolean m_isFakeConfiguration;
  private boolean m_firstTimeOnly = false;
  private boolean m_lastTimeOnly = false;

  public ConfigurationAnnotation() {
  }

  public void setIsBeforeGroups(boolean isBeforeGroups) {
    m_isBeforeGroups = isBeforeGroups;
  }

  public void setIsAfterGroups(boolean isAfterGroups) {
    m_isAfterGroups = isAfterGroups;
  }

  @Override
  public boolean isBeforeGroups() {
    return m_isBeforeGroups;
  }

  @Override
  public boolean isAfterGroups() {
    return m_isAfterGroups;
  }

  @Override
  public boolean getBeforeTestClass() {
    return m_beforeTestClass;
  }

  public void setBeforeTestClass(boolean beforeTestClass) {
    m_beforeTestClass = beforeTestClass;
  }

  @Override
  public boolean getAfterTestClass() {
    return m_afterTestClass;
  }

  public void setAfterTestClass(boolean afterTestClass) {
    m_afterTestClass = afterTestClass;
  }

  @Override
  public boolean getBeforeTestMethod() {
    return m_beforeTestMethod;
  }

  public void setBeforeTestMethod(boolean beforeTestMethod) {
    m_beforeTestMethod = beforeTestMethod;
  }

  @Override
  public boolean getAfterTestMethod() {
    return m_afterTestMethod;
  }

  public void setAfterTestMethod(boolean afterTestMethod) {
    m_afterTestMethod = afterTestMethod;
  }

  @Override
  public boolean getBeforeSuite() {
    return m_beforeSuite;
  }

  public void setBeforeSuite(boolean beforeSuite) {
    m_beforeSuite = beforeSuite;
  }

  @Override
  public boolean getAfterSuite() {
    return m_afterSuite;
  }

  public void setAfterSuite(boolean afterSuite) {
    m_afterSuite = afterSuite;
  }

  @Override
  public boolean getBeforeTest() {
    return m_beforeTest;
  }

  public void setBeforeTest(boolean beforeTest) {
    m_beforeTest = beforeTest;
  }

  @Override
  public boolean getAfterTest() {
    return m_afterTest;
  }

  public void setAfterTest(boolean afterTest) {
    m_afterTest = afterTest;
  }

  @Override
  public boolean getAlwaysRun() {
    return m_alwaysRun;
  }

  public void setAlwaysRun(boolean alwaysRun) {
    m_alwaysRun = alwaysRun;
  }

  @Override
  public boolean getInheritGroups() {
    return m_inheritGroups;
  }

  public void setInheritGroups(boolean inheritGroups) {
    m_inheritGroups = inheritGroups;
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

  @Override
  public String[] getGroupFilters() {
    return m_groupFilters;
  }

  void setGroupFilters(String[] groupFilters) {
    m_groupFilters = groupFilters;
  }

  @Override
  public boolean isFakeConfiguration() {
    return m_isFakeConfiguration;
  }

  public void setFakeConfiguration(boolean b) {
    m_isFakeConfiguration = b;
  }

  public boolean isFirstTimeOnly() {
    return m_firstTimeOnly;
  }

  public void setFirstTimeOnly(boolean f) {
    m_firstTimeOnly = f;
  }

  public boolean isLastTimeOnly() {
    return m_lastTimeOnly;
  }

  public void setLastTimeOnly(boolean f) {
    m_lastTimeOnly = f;
  }
}
