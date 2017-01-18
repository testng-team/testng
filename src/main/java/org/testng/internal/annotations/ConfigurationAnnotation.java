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
  private boolean beforeTestClass = false;
  private boolean afterTestClass = false;
  private boolean beforeTestMethod = false;
  private boolean afterTestMethod = false;
  private boolean beforeTest = false;
  private boolean afterTest = false;
  private boolean beforeSuite = false;
  private boolean afterSuite = false;
  private String[] parameters = {};
  private boolean alwaysRun = false;
  private boolean inheritGroups = true;
  private String[] beforeGroups = {};
  private String[] afterGroups = {};
  private boolean isFakeConfiguration;
  private boolean firstTimeOnly = false;
  private boolean lastTimeOnly = false;

  public ConfigurationAnnotation() {

  }

  public void setAfterSuite(boolean afterSuite) {
    this.afterSuite = afterSuite;
  }

  public void setAfterTest(boolean afterTest) {
    this.afterTest = afterTest;
  }

  public void setAfterTestClass(boolean afterTestClass) {
    this.afterTestClass = afterTestClass;
  }

  public void setAfterTestMethod(boolean afterTestMethod) {
    this.afterTestMethod = afterTestMethod;
  }

  public void setAlwaysRun(boolean alwaysRun) {
    this.alwaysRun = alwaysRun;
  }

  public void setBeforeSuite(boolean beforeSuite) {
    this.beforeSuite = beforeSuite;
  }

  public void setBeforeTest(boolean beforeTest) {
    this.beforeTest = beforeTest;
  }

  public void setBeforeTestClass(boolean beforeTestClass) {
    this.beforeTestClass = beforeTestClass;
  }

  public void setBeforeTestMethod(boolean beforeTestMethod) {
    this.beforeTestMethod = beforeTestMethod;
  }

  public void setInheritGroups(boolean inheritGroups) {
    this.inheritGroups = inheritGroups;
  }

  @Override
  public void setParameters(String[] parameters) {
    this.parameters = parameters;
  }

  @Override
  public boolean getBeforeTestClass() {
    return beforeTestClass;
  }

  @Override
  public boolean getAfterTestClass() {
    return afterTestClass;
  }

  @Override
  public boolean getBeforeTestMethod() {
    return beforeTestMethod;
  }

  @Override
  public boolean getAfterTestMethod() {
    return afterTestMethod;
  }

  @Override
  public boolean getBeforeSuite() {
    return beforeSuite;
  }

  @Override
  public boolean getAfterSuite() {
    return afterSuite;
  }

  @Override
  public boolean getBeforeTest() {
    return beforeTest;
  }

  @Override
  public boolean getAfterTest() {
    return afterTest;
  }

  @Override
  public String[] getParameters() {
    return parameters;
  }

  @Override
  public boolean getAlwaysRun() {
    return alwaysRun;
  }

  @Override
  public boolean getInheritGroups() {
    return inheritGroups;
  }

  @Override
  public String[] getAfterGroups() {
    return afterGroups;
  }

  public void setAfterGroups(String[] afterGroups) {
    this.afterGroups = afterGroups;
  }

  @Override
  public String[] getBeforeGroups() {
    return beforeGroups;
  }

  public void setBeforeGroups(String[] beforeGroups) {
    this.beforeGroups = beforeGroups;
  }

  public void setFakeConfiguration(boolean b) {
    isFakeConfiguration = b;
  }

  @Override
  public boolean isFakeConfiguration() {
    return isFakeConfiguration;
  }

  public void setFirstTimeOnly(boolean f) {
    firstTimeOnly = f;
  }

  public boolean isFirstTimeOnly() {
    return firstTimeOnly;
  }

  public void setLastTimeOnly(boolean f) {
    lastTimeOnly = f;
  }

  public boolean isLastTimeOnly() {
    return lastTimeOnly;
  }
}
