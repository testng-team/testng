package org.testng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import org.testng.collections.Lists;
import org.testng.collections.Objects;
import org.testng.internal.*;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

/**
 * This class represents a test class: - The test methods - The configuration methods (test and
 * method) - The class file
 */
class TestClass extends NoOpTestClass implements ITestClass, ITestClassConfigInfo {

  private IAnnotationFinder annotationFinder = null;
  // The Strategy used to locate test methods (TestNG, JUnit, etc...)
  private ITestMethodFinder testMethodFinder = null;

  private IClass iClass = null;
  private String testName;
  private XmlTest xmlTest;
  private XmlClass xmlClass;
  private final ITestObjectFactory objectFactory;
  private final String m_errorMsgPrefix;

  private final IdentityHashMap<Object, List<ITestNGMethod>> beforeClassConfig =
      new IdentityHashMap<>();

  @Override
  public List<ITestNGMethod> getAllBeforeClassMethods() {
    return beforeClassConfig
        .values()
        .parallelStream()
        .reduce(
            (a, b) -> {
              List<ITestNGMethod> methodList = new ArrayList<>(a);
              methodList.addAll(b);
              return methodList;
            })
        .orElse(Lists.newArrayList());
  }

  @Override
  public List<ITestNGMethod> getInstanceBeforeClassMethods(Object instance) {
    return beforeClassConfig.get(instance);
  }

  private static final Logger LOG = Logger.getLogger(TestClass.class);

  protected TestClass(
      ITestObjectFactory objectFactory,
      IClass cls,
      ITestMethodFinder testMethodFinder,
      IAnnotationFinder annotationFinder,
      XmlTest xmlTest,
      XmlClass xmlClass,
      String errorMsgPrefix) {
    this.objectFactory = objectFactory;
    this.m_errorMsgPrefix = errorMsgPrefix;
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
    Object[] instances = getInstances(true, this.m_errorMsgPrefix);
    for (Object instance : instances) {
      instance = IParameterInfo.embeddedInstance(instance);
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
  public Object[] getInstances(boolean create, String errorMsgPrefix) {
    return iClass.getInstances(create, this.m_errorMsgPrefix);
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

    for (Object eachInstance : iClass.getInstances(false)) {
      m_beforeSuiteMethods =
          ConfigurationMethod.createSuiteConfigurationMethods(
              objectFactory,
              testMethodFinder.getBeforeSuiteMethods(m_testClass),
              annotationFinder,
              true,
              eachInstance);
      m_afterSuiteMethods =
          ConfigurationMethod.createSuiteConfigurationMethods(
              objectFactory,
              testMethodFinder.getAfterSuiteMethods(m_testClass),
              annotationFinder,
              false,
              eachInstance);
      m_beforeTestConfMethods =
          ConfigurationMethod.createTestConfigurationMethods(
              objectFactory,
              testMethodFinder.getBeforeTestConfigurationMethods(m_testClass),
              annotationFinder,
              true,
              this.xmlTest,
              eachInstance);
      m_afterTestConfMethods =
          ConfigurationMethod.createTestConfigurationMethods(
              objectFactory,
              testMethodFinder.getAfterTestConfigurationMethods(m_testClass),
              annotationFinder,
              false,
              this.xmlTest,
              eachInstance);
      m_beforeClassMethods =
          ConfigurationMethod.createClassConfigurationMethods(
              objectFactory,
              testMethodFinder.getBeforeClassMethods(m_testClass),
              annotationFinder,
              true,
              xmlTest,
              eachInstance);
      Object instance = IParameterInfo.embeddedInstance(eachInstance);
      beforeClassConfig.put(instance, Arrays.asList(m_beforeClassMethods));
      m_afterClassMethods =
          ConfigurationMethod.createClassConfigurationMethods(
              objectFactory,
              testMethodFinder.getAfterClassMethods(m_testClass),
              annotationFinder,
              false,
              xmlTest,
              eachInstance);
      m_beforeGroupsMethods =
          ConfigurationMethod.createBeforeConfigurationMethods(
              objectFactory,
              testMethodFinder.getBeforeGroupsConfigurationMethods(m_testClass),
              annotationFinder,
              true,
              eachInstance);
      m_afterGroupsMethods =
          ConfigurationMethod.createAfterConfigurationMethods(
              objectFactory,
              testMethodFinder.getAfterGroupsConfigurationMethods(m_testClass),
              annotationFinder,
              false,
              eachInstance);
      m_beforeTestMethods =
          ConfigurationMethod.createTestMethodConfigurationMethods(
              objectFactory,
              testMethodFinder.getBeforeTestMethods(m_testClass),
              annotationFinder,
              true,
              xmlTest,
              eachInstance);
      m_afterTestMethods =
          ConfigurationMethod.createTestMethodConfigurationMethods(
              objectFactory,
              testMethodFinder.getAfterTestMethods(m_testClass),
              annotationFinder,
              false,
              xmlTest,
              eachInstance);
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
          vResult.add(new TestNGMethod(objectFactory, m.getMethod(), annotationFinder, xmlTest, o));
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
