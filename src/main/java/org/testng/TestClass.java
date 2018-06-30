package org.testng;

import org.testng.collections.Lists;
import org.testng.collections.Objects;
import org.testng.internal.*;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import java.util.List;

/**
 * This class represents a test class: - The test methods - The configuration methods (test and
 * method) - The class file
 */
class TestClass extends NoOpTestClass implements ITestClass {

  private IAnnotationFinder annotationFinder = null;
  // The Strategy used to locate test methods (TestNG, JUnit, etc...)
  private ITestMethodFinder testMethodFinder = null;

  private IClass iClass = null;
  private String testName;
  private XmlTest xmlTest;
  private XmlClass xmlClass;

  private static final Logger LOG = Logger.getLogger(TestClass.class);

  protected TestClass(
      IClass cls,
      ITestMethodFinder testMethodFinder,
      IAnnotationFinder annotationFinder,
      XmlTest xmlTest,
      XmlClass xmlClass) {
    init(cls, testMethodFinder, annotationFinder, xmlTest, xmlClass);
  }

  @Override
  public String getTestName() {
    return testName;
  }

  @Override
  public XmlTest getXmlTest() {
    return xmlTest;
  }

  @Override
  public XmlClass getXmlClass() {
    return xmlClass;
  }

  public IAnnotationFinder getAnnotationFinder() {
    return annotationFinder;
  }

  private void init(
      IClass cls,
      ITestMethodFinder testMethodFinder,
      IAnnotationFinder annotationFinder,
      XmlTest xmlTest,
      XmlClass xmlClass) {
    log(3, "Creating TestClass for " + cls);
    iClass = cls;
    m_testClass = cls.getRealClass();
    this.xmlTest = xmlTest;
    this.xmlClass = xmlClass;
    this.testMethodFinder = testMethodFinder;
    this.annotationFinder = annotationFinder;
    initTestClassesAndInstances();
    initMethods();
  }

  private void initTestClassesAndInstances() {
    //
    // TestClasses and instances
    //
    Object[] instances = getInstances(true);
    for (Object instance : instances) {
      if (instance instanceof ITest) {
        testName = ((ITest) instance).getTestName();
        break;
      }
    }
    if (testName == null) {
      testName = iClass.getTestName();
    }
  }

  @Override
  public Object[] getInstances(boolean create) {
    return iClass.getInstances(create);
  }

  @Override
  public long[] getInstanceHashCodes() {
    return iClass.getInstanceHashCodes();
  }

  @Override
  public void addInstance(Object instance) {
    iClass.addInstance(instance);
  }

  private void initMethods() {
    ITestNGMethod[] methods = testMethodFinder.getTestMethods(m_testClass, xmlTest);
    m_testMethods = createTestMethods(methods);

    for (Object instance : iClass.getInstances(false)) {
      m_beforeSuiteMethods =
          ConfigurationMethod.createSuiteConfigurationMethods(
              testMethodFinder.getBeforeSuiteMethods(m_testClass),
              annotationFinder,
              true,
              instance);
      m_afterSuiteMethods =
          ConfigurationMethod.createSuiteConfigurationMethods(
              testMethodFinder.getAfterSuiteMethods(m_testClass),
              annotationFinder,
              false,
              instance);
      m_beforeTestConfMethods =
          ConfigurationMethod.createTestConfigurationMethods(
              testMethodFinder.getBeforeTestConfigurationMethods(m_testClass),
              annotationFinder,
              true,
              instance);
      m_afterTestConfMethods =
          ConfigurationMethod.createTestConfigurationMethods(
              testMethodFinder.getAfterTestConfigurationMethods(m_testClass),
              annotationFinder,
              false,
              instance);
      m_beforeClassMethods =
          ConfigurationMethod.createClassConfigurationMethods(
              testMethodFinder.getBeforeClassMethods(m_testClass),
              annotationFinder,
              true,
              instance);
      m_afterClassMethods =
          ConfigurationMethod.createClassConfigurationMethods(
              testMethodFinder.getAfterClassMethods(m_testClass),
              annotationFinder,
              false,
              instance);
      m_beforeGroupsMethods =
          ConfigurationMethod.createBeforeConfigurationMethods(
              testMethodFinder.getBeforeGroupsConfigurationMethods(m_testClass),
              annotationFinder,
              true,
              instance);
      m_afterGroupsMethods =
          ConfigurationMethod.createAfterConfigurationMethods(
              testMethodFinder.getAfterGroupsConfigurationMethods(m_testClass),
              annotationFinder,
              false,
              instance);
      m_beforeTestMethods =
          ConfigurationMethod.createTestMethodConfigurationMethods(
              testMethodFinder.getBeforeTestMethods(m_testClass), annotationFinder, true, instance);
      m_afterTestMethods =
          ConfigurationMethod.createTestMethodConfigurationMethods(
              testMethodFinder.getAfterTestMethods(m_testClass), annotationFinder, false, instance);
    }
  }

  /**
   * Create the test methods that belong to this class (rejects all those that belong to a different
   * class).
   */
  private ITestNGMethod[] createTestMethods(ITestNGMethod[] methods) {
    List<ITestNGMethod> vResult = Lists.newArrayList();
    for (ITestNGMethod tm : methods) {
      ConstructorOrMethod m = tm.getConstructorOrMethod();
      if (m.getDeclaringClass().isAssignableFrom(m_testClass)) {
        for (Object o : iClass.getInstances(false)) {
          log(4, "Adding method " + tm + " on TestClass " + m_testClass);
          vResult.add(
              new TestNGMethod(
                  /* tm.getRealClass(), */ m.getMethod(), annotationFinder, xmlTest, o));
        }
      } else {
        log(4, "Rejecting method " + tm + " for TestClass " + m_testClass);
      }
    }

    return vResult.toArray(new ITestNGMethod[0]);
  }

  public ITestMethodFinder getTestMethodFinder() {
    return testMethodFinder;
  }

  private void log(int level, String s) {
    Utils.log("TestClass", level, s);
  }

  protected void dump() {
    LOG.info("===== Test class\n" + m_testClass.getName());
    for (ITestNGMethod m : m_beforeClassMethods) {
      LOG.info("  @BeforeClass " + m);
    }
    for (ITestNGMethod m : m_beforeTestMethods) {
      LOG.info("  @BeforeMethod " + m);
    }
    for (ITestNGMethod m : m_testMethods) {
      LOG.info("    @Test " + m);
    }
    for (ITestNGMethod m : m_afterTestMethods) {
      LOG.info("  @AfterMethod " + m);
    }
    for (ITestNGMethod m : m_afterClassMethods) {
      LOG.info("  @AfterClass " + m);
    }
    LOG.info("======");
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(getClass()).add("name", m_testClass).toString();
  }

  public IClass getIClass() {
    return iClass;
  }
}
