package org.testng.internal;

import java.util.ArrayList;
import java.util.List;
import org.testng.ITestClass;
import org.testng.ITestNGMethod;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

public class NoOpTestClass implements ITestClass, IObject {

  protected Class<?> m_testClass = null;

  // Test methods
  protected List<ITestNGMethod> m_beforeClassMethods = new ArrayList<>();
  protected List<ITestNGMethod> m_beforeTestMethods = new ArrayList<>();
  protected ITestNGMethod[] m_testMethods = new ITestNGMethod[0];
  protected List<ITestNGMethod> m_afterClassMethods = new ArrayList<>();
  protected List<ITestNGMethod> m_afterTestMethods = new ArrayList<>();
  protected List<ITestNGMethod> m_beforeSuiteMethods = new ArrayList<>();
  protected List<ITestNGMethod> m_afterSuiteMethods = new ArrayList<>();
  protected List<ITestNGMethod> m_beforeTestConfMethods = new ArrayList<>();
  protected List<ITestNGMethod> m_afterTestConfMethods = new ArrayList<>();
  protected ITestNGMethod[] m_beforeGroupsMethods = new ITestNGMethod[0];
  protected List<ITestNGMethod> m_afterGroupsMethods = new ArrayList<>();

  private final IdentifiableObject[] m_instances;
  private final long[] m_instanceHashes;

  private final XmlTest m_xmlTest;
  private final XmlClass m_xmlClass;

  protected NoOpTestClass() {
    m_instances = null;
    m_instanceHashes = null;
    m_xmlTest = null;
    m_xmlClass = null;
  }

  public NoOpTestClass(ITestClass testClass) {
    m_testClass = testClass.getRealClass();
    m_beforeSuiteMethods = List.of(testClass.getBeforeSuiteMethods());
    m_beforeTestConfMethods = List.of(testClass.getBeforeTestConfigurationMethods());
    m_beforeGroupsMethods = testClass.getBeforeGroupsMethods();
    m_beforeClassMethods = List.of(testClass.getBeforeClassMethods());
    m_beforeTestMethods = List.of(testClass.getBeforeTestMethods());
    m_afterSuiteMethods = List.of(testClass.getAfterSuiteMethods());
    m_afterTestConfMethods = List.of(testClass.getAfterTestConfigurationMethods());
    m_afterGroupsMethods = List.of(testClass.getAfterGroupsMethods());
    m_afterClassMethods = List.of(testClass.getAfterClassMethods());
    m_afterTestMethods = List.of(testClass.getAfterTestMethods());
    m_instances = IObject.objects(testClass, true);
    m_instanceHashes = IObject.instanceHashCodes(testClass);
    m_xmlTest = testClass.getXmlTest();
    m_xmlClass = testClass.getXmlClass();
  }

  public void setBeforeTestMethods(ITestNGMethod[] beforeTestMethods) {
    m_beforeTestMethods = List.of(beforeTestMethods);
  }

  public void setAfterTestMethod(ITestNGMethod[] afterTestMethods) {
    m_afterTestMethods = List.of(afterTestMethods);
  }

  /** @return Returns the afterClassMethods. */
  @Override
  public ITestNGMethod[] getAfterClassMethods() {
    return m_afterClassMethods.toArray(ITestNGMethod[]::new);
  }

  /** @return Returns the afterTestMethods. */
  @Override
  public ITestNGMethod[] getAfterTestMethods() {
    return m_afterTestMethods.toArray(ITestNGMethod[]::new);
  }

  /** @return Returns the beforeClassMethods. */
  @Override
  public ITestNGMethod[] getBeforeClassMethods() {
    return m_beforeClassMethods.toArray(ITestNGMethod[]::new);
  }

  /** @return Returns the beforeTestMethods. */
  @Override
  public ITestNGMethod[] getBeforeTestMethods() {
    return m_beforeTestMethods.toArray(ITestNGMethod[]::new);
  }

  /** @return Returns the testMethods. */
  @Override
  public ITestNGMethod[] getTestMethods() {
    return m_testMethods;
  }

  @Override
  public ITestNGMethod[] getBeforeSuiteMethods() {
    return m_beforeSuiteMethods.toArray(ITestNGMethod[]::new);
  }

  @Override
  public ITestNGMethod[] getAfterSuiteMethods() {
    return m_afterSuiteMethods.toArray(ITestNGMethod[]::new);
  }

  @Override
  public ITestNGMethod[] getBeforeTestConfigurationMethods() {
    return m_beforeTestConfMethods.toArray(ITestNGMethod[]::new);
  }

  @Override
  public ITestNGMethod[] getAfterTestConfigurationMethods() {
    return m_afterTestConfMethods.toArray(ITestNGMethod[]::new);
  }

  /** @return all @Configuration methods that should be invoked before certain groups */
  @Override
  public ITestNGMethod[] getBeforeGroupsMethods() {
    return m_beforeGroupsMethods;
  }

  /** @return all @Configuration methods that should be invoked after certain groups */
  @Override
  public ITestNGMethod[] getAfterGroupsMethods() {
    return m_afterGroupsMethods.toArray(ITestNGMethod[]::new);
  }

  /** @see org.testng.internal.IObject#getInstanceHashCodes() */
  @Override
  public long[] getInstanceHashCodes() {
    return m_instanceHashes;
  }

  @Override
  public Object[] getInstances(boolean reuse) {
    return m_instances;
  }

  @Override
  public String getName() {
    return m_testClass.getName();
  }

  @Override
  public Class<?> getRealClass() {
    return m_testClass;
  }

  @Override
  public void addInstance(Object instance) {}

  @Override
  public void addObject(IdentifiableObject instance) {}

  @Override
  public IdentifiableObject[] getObjects(boolean create, String errorMsgPrefix) {
    return m_instances;
  }

  public void setTestClass(Class<?> declaringClass) {
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
