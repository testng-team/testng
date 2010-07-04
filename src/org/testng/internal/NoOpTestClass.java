package org.testng.internal;


import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

/**
 * This class/interface
 */
public class NoOpTestClass implements ITestClass {
  protected Class m_testClass= null;

  // Test methods
  protected ITestNGMethod[] m_beforeClassMethods= null;
  protected ITestNGMethod[] m_beforeTestMethods= null;
  protected ITestNGMethod[] m_testMethods= null;
  protected ITestNGMethod[] m_afterClassMethods= null;
  protected ITestNGMethod[] m_afterTestMethods= null;
  protected ITestNGMethod[] m_beforeSuiteMethods= null;
  protected ITestNGMethod[] m_afterSuiteMethods= null;
  protected ITestNGMethod[] m_beforeTestConfMethods= null;
  protected ITestNGMethod[] m_afterTestConfMethods= null;
  protected ITestNGMethod[] m_beforeGroupsMethods= null;
  protected ITestNGMethod[] m_afterGroupsMethods= null;

  private Object[] m_instances;
  private long[] m_instanceHashes;

  private XmlTest m_xmlTest;

  private XmlClass m_xmlClass;

  protected NoOpTestClass() {
  }

  public NoOpTestClass(ITestClass testClass) {
    m_testClass= testClass.getRealClass();
    m_beforeSuiteMethods= testClass.getBeforeSuiteMethods();
    m_beforeTestConfMethods= testClass.getBeforeTestConfigurationMethods();
    m_beforeGroupsMethods= testClass.getBeforeGroupsMethods();
    m_beforeClassMethods= testClass.getBeforeClassMethods();
    m_beforeTestMethods= testClass.getBeforeTestMethods();
    m_afterSuiteMethods= testClass.getAfterSuiteMethods();
    m_afterTestConfMethods= testClass.getAfterTestConfigurationMethods();
    m_afterGroupsMethods= testClass.getAfterGroupsMethods();
    m_afterClassMethods= testClass.getAfterClassMethods();
    m_afterTestMethods= testClass.getAfterTestMethods();
    m_instances= testClass.getInstances(true);
    m_instanceHashes= testClass.getInstanceHashCodes();
    m_xmlTest = testClass.getXmlTest();
    m_xmlClass = testClass.getXmlClass();
  }

  public void setBeforeTestMethods(ITestNGMethod[] beforeTestMethods) {
    m_beforeTestMethods= beforeTestMethods;
  }

  public void setAfterTestMethod(ITestNGMethod[] afterTestMethods) {
    m_afterTestMethods= afterTestMethods;
  }

  /**
   * @return Returns the afterClassMethods.
   */
  public ITestNGMethod[] getAfterClassMethods() {
    return m_afterClassMethods;
  }

  /**
   * @return Returns the afterTestMethods.
   */
  public ITestNGMethod[] getAfterTestMethods() {
    return m_afterTestMethods;
  }

  /**
   * @return Returns the beforeClassMethods.
   */
  public ITestNGMethod[] getBeforeClassMethods() {
    return m_beforeClassMethods;
  }

  /**
   * @return Returns the beforeTestMethods.
   */
  public ITestNGMethod[] getBeforeTestMethods() {
    return m_beforeTestMethods;
  }

  /**
   * @return Returns the testMethods.
   */
  public ITestNGMethod[] getTestMethods() {
    return m_testMethods;
  }

  public ITestNGMethod[] getBeforeSuiteMethods() {
    return m_beforeSuiteMethods;
  }

  public ITestNGMethod[] getAfterSuiteMethods() {
    return m_afterSuiteMethods;
  }

  public ITestNGMethod[] getBeforeTestConfigurationMethods() {
    return m_beforeTestConfMethods;
  }

  public ITestNGMethod[] getAfterTestConfigurationMethods() {
    return m_afterTestConfMethods;
  }

  /**
   * @return all @Configuration methods that should be invoked before certain groups
   */
  public ITestNGMethod[] getBeforeGroupsMethods() {
    return m_beforeGroupsMethods;
  }

  /**
   * @return all @Configuration methods that should be invoked after certain groups
   */
  public ITestNGMethod[] getAfterGroupsMethods() {
    return m_afterGroupsMethods;
  }

  /**
   * @see org.testng.ITestClass#getInstanceCount()
   */
  public int getInstanceCount() {
    return m_instances.length;
  }

  /**
   * @see org.testng.ITestClass#getInstanceHashCodes()
   */
  public long[] getInstanceHashCodes() {
    return m_instanceHashes;
  }

  /**
   * @see org.testng.ITestClass#getInstances(boolean)
   */
  public Object[] getInstances(boolean reuse) {
    return m_instances;
  }

  public String getName() {
    return m_testClass.getName();
  }

  public Class getRealClass() {
    return m_testClass;
  }

  /**
   * @see org.testng.IClass#addInstance(java.lang.Object)
   */
  public void addInstance(Object instance) {
  }

  public void setTestClass(Class< ? > declaringClass) {
    m_testClass = declaringClass;
  }

  public String getTestName() {
    // TODO Auto-generated method stub
    return null;
  }

  public XmlTest getXmlTest() {
    return m_xmlTest;
  }

  public XmlClass getXmlClass() {
    return m_xmlClass;
  }
}
