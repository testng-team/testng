package org.testng.internal;


import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

public class NoOpTestClass implements ITestClass {
  private static final long serialVersionUID = -4544061405329040593L;

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

  private transient Object[] m_instances;
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
  @Override
  public ITestNGMethod[] getAfterClassMethods() {
    return m_afterClassMethods;
  }

  /**
   * @return Returns the afterTestMethods.
   */
  @Override
  public ITestNGMethod[] getAfterTestMethods() {
    return m_afterTestMethods;
  }

  /**
   * @return Returns the beforeClassMethods.
   */
  @Override
  public ITestNGMethod[] getBeforeClassMethods() {
    return m_beforeClassMethods;
  }

  /**
   * @return Returns the beforeTestMethods.
   */
  @Override
  public ITestNGMethod[] getBeforeTestMethods() {
    return m_beforeTestMethods;
  }

  /**
   * @return Returns the testMethods.
   */
  @Override
  public ITestNGMethod[] getTestMethods() {
    return m_testMethods;
  }

  @Override
  public ITestNGMethod[] getBeforeSuiteMethods() {
    return m_beforeSuiteMethods;
  }

  @Override
  public ITestNGMethod[] getAfterSuiteMethods() {
    return m_afterSuiteMethods;
  }

  @Override
  public ITestNGMethod[] getBeforeTestConfigurationMethods() {
    return m_beforeTestConfMethods;
  }

  @Override
  public ITestNGMethod[] getAfterTestConfigurationMethods() {
    return m_afterTestConfMethods;
  }

  /**
   * @return all @Configuration methods that should be invoked before certain groups
   */
  @Override
  public ITestNGMethod[] getBeforeGroupsMethods() {
    return m_beforeGroupsMethods;
  }

  /**
   * @return all @Configuration methods that should be invoked after certain groups
   */
  @Override
  public ITestNGMethod[] getAfterGroupsMethods() {
    return m_afterGroupsMethods;
  }

  /**
   * @see org.testng.ITestClass#getInstanceCount()
   */
  @Override
  public int getInstanceCount() {
    return m_instances.length;
  }

  /**
   * @see org.testng.ITestClass#getInstanceHashCodes()
   */
  @Override
  public long[] getInstanceHashCodes() {
    return m_instanceHashes;
  }

  /**
   * @see org.testng.ITestClass#getInstances(boolean)
   */
  @Override
  public Object[] getInstances(boolean reuse) {
    return m_instances;
  }

  @Override
  public String getName() {
    return m_testClass.getName();
  }

  @Override
  public Class getRealClass() {
    return m_testClass;
  }

  /**
   * @see org.testng.IClass#addInstance(java.lang.Object)
   */
  @Override
  public void addInstance(Object instance) {
  }

  public void setTestClass(Class< ? > declaringClass) {
    m_testClass = declaringClass;
  }

  @Override
  public String getTestName() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public XmlTest getXmlTest() {
    return m_xmlTest;
  }

  @Override
  public XmlClass getXmlClass() {
    return m_xmlClass;
  }
}
