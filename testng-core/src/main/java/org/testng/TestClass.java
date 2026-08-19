package org.testng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.testng.collections.Objects;
import org.testng.internal.ConfigurationMethod;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.IObject;
import org.testng.internal.IParameterInfo;
import org.testng.internal.ITestClassConfigInfo;
import org.testng.internal.NoOpTestClass;
import org.testng.internal.TestNGMethod;
import org.testng.internal.Utils;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

/**
 * This class represents a test class: - The test methods - The configuration methods (test and
 * method) - The class file
 */
class TestClass extends NoOpTestClass implements ITestClass, ITestClassConfigInfo, IObject {

  private IAnnotationFinder annotationFinder;
  // The Strategy used to locate test methods (TestNG, JUnit, etc...)
  private ITestMethodFinder testMethodFinder;

  private IClass iClass;
  private @Nullable String testName;
  private XmlTest xmlTest;
  private @Nullable XmlClass xmlClass;
  private final ITestObjectFactory objectFactory;
  private final @Nullable String m_errorMsgPrefix;

  // Keyed by the per-instance id (UUID) rather than the instantiated instance so that binding
  // per-instance @BeforeClass/@AfterClass methods never forces a lazy @Factory instance to be
  // created during collection.
  private final Map<UUID, List<ITestNGMethod>> beforeClassConfig = new LinkedHashMap<>();

  private final Map<UUID, List<ITestNGMethod>> afterClassConfig = new LinkedHashMap<>();

  @Override
  public List<ITestNGMethod> getAllBeforeClassMethods() {
    return getAllClassLevelConfigs(beforeClassConfig);
  }

  @Override
  public List<ITestNGMethod> getAllAfterClassMethods() {
    return getAllClassLevelConfigs(afterClassConfig);
  }

  private static List<ITestNGMethod> getAllClassLevelConfigs(Map<UUID, List<ITestNGMethod>> map) {
    return map.values()
        .parallelStream()
        .reduce(
            (a, b) -> {
              List<ITestNGMethod> methodList = new ArrayList<>(a);
              methodList.addAll(b);
              return methodList;
            })
        .orElse(new ArrayList<>());
  }

  @Override
  public List<ITestNGMethod> getInstanceBeforeClassMethods(@Nullable UUID instanceId) {
    List<ITestNGMethod> methods = beforeClassConfig.get(instanceId);
    return methods == null ? new ArrayList<>() : methods;
  }

  @Override
  public List<ITestNGMethod> getInstanceAfterClassMethods(@Nullable UUID instanceId) {
    List<ITestNGMethod> methods = afterClassConfig.get(instanceId);
    return methods == null ? new ArrayList<>() : methods;
  }

  /**
   * The real class this TestClass was built for; {@code init} binds it before anything reads it.
   */
  private Class<?> realClass() {
    return java.util.Objects.requireNonNull(m_testClass, "a TestClass is bound to its real class");
  }

  private static final Logger LOG = Logger.getLogger(TestClass.class);

  protected TestClass(
      ITestObjectFactory objectFactory,
      IClass cls,
      ITestMethodFinder testMethodFinder,
      IAnnotationFinder annotationFinder,
      XmlTest xmlTest,
      @Nullable XmlClass xmlClass,
      @Nullable String errorMsgPrefix) {
    this.objectFactory = objectFactory;
    this.m_errorMsgPrefix = errorMsgPrefix;
    init(cls, testMethodFinder, annotationFinder, xmlTest, xmlClass);
  }

  @Override
  public @Nullable String getTestName() {
    return testName;
  }

  @Override
  public XmlTest getXmlTest() {
    return xmlTest;
  }

  @Override
  public @Nullable XmlClass getXmlClass() {
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
      @Nullable XmlClass xmlClass) {
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
    IObject.IdentifiableObject[] instances = getObjects(true, this.m_errorMsgPrefix);
    Arrays.stream(instances)
        .map(IdentifiableObject::getInstance)
        // Only inspect instances that already exist; a lazy @Factory instance must not be created
        // just to look up an ITest name. Such instances fall back to the class/xml test name below.
        .filter(TestClass::isInstantiated)
        .map(IParameterInfo::embeddedInstance)
        .filter(it -> it instanceof ITest)
        .findFirst()
        .ifPresent(it -> testName = ((ITest) it).getTestName());
    if (testName == null) {
      testName = iClass.getTestName();
    }
  }

  @Override
  public Object[] getInstances(boolean create) {
    return iClass.getInstances(create);
  }

  @Override
  public Object[] getInstances(boolean create, @Nullable String errorMsgPrefix) {
    return iClass.getInstances(create, this.m_errorMsgPrefix);
  }

  @Override
  public IObject.IdentifiableObject[] getObjects(boolean create, @Nullable String errorMsgPrefix) {
    return IObject.objects(iClass, create, errorMsgPrefix);
  }

  @Override
  public long[] getInstanceHashCodes() {
    return IObject.instanceHashCodes(iClass);
  }

  @Override
  public void addInstance(Object instance) {
    iClass.addInstance(instance);
  }

  @Override
  public void addObject(IObject.IdentifiableObject instance) {
    IObject.cast(iClass).ifPresent(it -> it.addObject(instance));
  }

  private void initMethods() {
    ITestNGMethod[] methods = testMethodFinder.getTestMethods(realClass(), xmlTest);
    m_testMethods = createTestMethods(methods);

    for (IdentifiableObject eachInstance : IObject.objects(iClass, false)) {
      m_beforeSuiteMethods =
          ConfigurationMethod.createSuiteConfigurationMethods(
              objectFactory,
              testMethodFinder.getBeforeSuiteMethods(realClass()),
              annotationFinder,
              true,
              eachInstance);
      m_afterSuiteMethods =
          ConfigurationMethod.createSuiteConfigurationMethods(
              objectFactory,
              testMethodFinder.getAfterSuiteMethods(realClass()),
              annotationFinder,
              false,
              eachInstance);
      m_beforeTestConfMethods =
          ConfigurationMethod.createTestConfigurationMethods(
              objectFactory,
              testMethodFinder.getBeforeTestConfigurationMethods(realClass()),
              annotationFinder,
              true,
              this.xmlTest,
              eachInstance);
      m_afterTestConfMethods =
          ConfigurationMethod.createTestConfigurationMethods(
              objectFactory,
              testMethodFinder.getAfterTestConfigurationMethods(realClass()),
              annotationFinder,
              false,
              this.xmlTest,
              eachInstance);
      m_beforeClassMethods =
          ConfigurationMethod.createClassConfigurationMethods(
              objectFactory,
              testMethodFinder.getBeforeClassMethods(realClass()),
              annotationFinder,
              true,
              xmlTest,
              eachInstance);
      beforeClassConfig.put(eachInstance.getInstanceId(), m_beforeClassMethods);
      m_afterClassMethods =
          ConfigurationMethod.createClassConfigurationMethods(
              objectFactory,
              testMethodFinder.getAfterClassMethods(realClass()),
              annotationFinder,
              false,
              xmlTest,
              eachInstance);
      afterClassConfig.put(eachInstance.getInstanceId(), m_afterClassMethods);
      m_beforeGroupsMethods =
          ConfigurationMethod.createBeforeConfigurationMethods(
              objectFactory,
              testMethodFinder.getBeforeGroupsConfigurationMethods(realClass()),
              annotationFinder,
              true,
              eachInstance);
      m_afterGroupsMethods =
          ConfigurationMethod.createAfterConfigurationMethods(
              objectFactory,
              testMethodFinder.getAfterGroupsConfigurationMethods(realClass()),
              annotationFinder,
              false,
              eachInstance);
      m_beforeTestMethods.addAll(
          ConfigurationMethod.createTestMethodConfigurationMethods(
              objectFactory,
              testMethodFinder.getBeforeTestMethods(realClass()),
              annotationFinder,
              true,
              xmlTest,
              eachInstance));
      m_afterTestMethods.addAll(
          ConfigurationMethod.createTestMethodConfigurationMethods(
              objectFactory,
              testMethodFinder.getAfterTestMethods(realClass()),
              annotationFinder,
              false,
              xmlTest,
              eachInstance));
    }
  }

  /**
   * Create the test methods that belong to this class (rejects all those that belong to a different
   * class).
   */
  private ITestNGMethod[] createTestMethods(ITestNGMethod[] methods) {
    List<ITestNGMethod> vResult = new ArrayList<>();
    for (ITestNGMethod tm : methods) {
      ConstructorOrMethod m = tm.getConstructorOrMethod();
      if (m.getDeclaringClass().isAssignableFrom(realClass())) {
        for (IdentifiableObject o : IObject.objects(iClass, false)) {
          log(4, "Adding method " + tm + " on TestClass " + realClass());
          vResult.add(
              new TestNGMethod(objectFactory, m.requireMethod(), annotationFinder, xmlTest, o));
        }
      } else {
        log(4, "Rejecting method " + tm + " for TestClass " + realClass());
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
    LOG.info("===== Test class\n" + realClass().getName());
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
    return Objects.toStringHelper(getClass()).add("name", realClass()).toString();
  }

  public IClass getIClass() {
    return iClass;
  }

  private static boolean isInstantiated(Object instance) {
    if (instance instanceof IParameterInfo) {
      return ((IParameterInfo) instance).isInstanceInstantiated();
    }
    return true;
  }
}
