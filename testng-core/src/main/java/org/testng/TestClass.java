package org.testng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
import org.testng.internal.XmlMethodSelector;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.collections.Pair;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
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
  // Every <class> tag naming this class, in XML order. The tag may be repeated, and each repeat is
  // a separate run of the class's methods with its own parameters.
  private List<XmlClass> xmlClasses = Collections.emptyList();
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
    return beforeClassConfig.getOrDefault(instanceId, Collections.emptyList());
  }

  @Override
  public List<ITestNGMethod> getInstanceAfterClassMethods(@Nullable UUID instanceId) {
    return afterClassConfig.getOrDefault(instanceId, Collections.emptyList());
  }

  private static final Logger LOG = Logger.getLogger(TestClass.class);

  protected TestClass(
      ITestObjectFactory objectFactory,
      IClass cls,
      ITestMethodFinder testMethodFinder,
      IAnnotationFinder annotationFinder,
      XmlTest xmlTest,
      List<XmlClass> xmlClasses,
      @Nullable String errorMsgPrefix) {
    this.objectFactory = objectFactory;
    this.m_errorMsgPrefix = errorMsgPrefix;
    init(cls, testMethodFinder, annotationFinder, xmlTest, xmlClasses);
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
    // Cannot express more than one occurrence, so it answers the last tag, as ClassInfoMap does.
    return xmlClasses.isEmpty() ? null : xmlClasses.get(xmlClasses.size() - 1);
  }

  public IAnnotationFinder getAnnotationFinder() {
    return annotationFinder;
  }

  private void init(
      IClass cls,
      ITestMethodFinder testMethodFinder,
      IAnnotationFinder annotationFinder,
      XmlTest xmlTest,
      List<XmlClass> xmlClasses) {
    log(3, "Creating TestClass for " + cls);
    iClass = cls;
    m_testClass = cls.getRealClass();
    this.xmlTest = xmlTest;
    this.xmlClasses = xmlClasses;
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

  @Deprecated
  @Override
  public Object[] getInstances(boolean create) {
    return iClass.getInstances(create);
  }

  @Deprecated
  @Override
  public Object[] getInstances(boolean create, @Nullable String errorMsgPrefix) {
    return iClass.getInstances(create, this.m_errorMsgPrefix);
  }

  @Override
  public IObject.IdentifiableObject[] getObjects(boolean create, @Nullable String errorMsgPrefix) {
    return IObject.objects(iClass, create, errorMsgPrefix);
  }

  @Override
  public long[] getObjectHashCodes() {
    return IObject.objectHashCodes(iClass);
  }

  @Deprecated
  @Override
  public void addInstance(Object instance) {
    iClass.addInstance(instance);
  }

  @Override
  public void addObject(IObject.IdentifiableObject instance) {
    IObject.cast(iClass).ifPresent(it -> it.addObject(instance));
  }

  private void initMethods() {
    Class<?> realClass = getRealClass();
    ITestNGMethod[] methods = testMethodFinder.getTestMethods(realClass, xmlTest);
    IdentifiableObject[] instances = IObject.objects(iClass, false);
    m_testMethods = createTestMethods(methods, instances);
    if (instances.length == 0) {
      return;
    }

    // Every one of these lookups rescans the whole class hierarchy and none of them depends on the
    // instance, so look each configuration category up once for the test class and bind the
    // templates it answers to each instance in turn. A @Factory used to pay for all ten of them
    // once per instance it produced.
    ITestNGMethod[] beforeSuiteTemplates = testMethodFinder.getBeforeSuiteMethods(realClass);
    ITestNGMethod[] afterSuiteTemplates = testMethodFinder.getAfterSuiteMethods(realClass);
    ITestNGMethod[] beforeTestTemplates =
        testMethodFinder.getBeforeTestConfigurationMethods(realClass);
    ITestNGMethod[] afterTestTemplates =
        testMethodFinder.getAfterTestConfigurationMethods(realClass);
    ITestNGMethod[] beforeClassTemplates = testMethodFinder.getBeforeClassMethods(realClass);
    ITestNGMethod[] afterClassTemplates = testMethodFinder.getAfterClassMethods(realClass);
    ITestNGMethod[] beforeGroupsTemplates =
        testMethodFinder.getBeforeGroupsConfigurationMethods(realClass);
    ITestNGMethod[] afterGroupsTemplates =
        testMethodFinder.getAfterGroupsConfigurationMethods(realClass);
    ITestNGMethod[] beforeMethodTemplates = testMethodFinder.getBeforeTestMethods(realClass);
    ITestNGMethod[] afterMethodTemplates = testMethodFinder.getAfterTestMethods(realClass);

    IdentifiableObject prototypeInstance = instances[0];
    List<ITestNGMethod> beforeSuitePrototypes =
        ConfigurationMethod.createSuiteConfigurationMethods(
            objectFactory, beforeSuiteTemplates, annotationFinder, true, prototypeInstance);
    List<ITestNGMethod> afterSuitePrototypes =
        ConfigurationMethod.createSuiteConfigurationMethods(
            objectFactory, afterSuiteTemplates, annotationFinder, false, prototypeInstance);
    List<ITestNGMethod> beforeTestPrototypes =
        ConfigurationMethod.createTestConfigurationMethods(
            objectFactory,
            beforeTestTemplates,
            annotationFinder,
            true,
            this.xmlTest,
            prototypeInstance);
    List<ITestNGMethod> afterTestPrototypes =
        ConfigurationMethod.createTestConfigurationMethods(
            objectFactory,
            afterTestTemplates,
            annotationFinder,
            false,
            this.xmlTest,
            prototypeInstance);
    List<ITestNGMethod> beforeClassPrototypes =
        ConfigurationMethod.createClassConfigurationMethods(
            objectFactory, beforeClassTemplates, annotationFinder, true, xmlTest, prototypeInstance);
    List<ITestNGMethod> afterClassPrototypes =
        ConfigurationMethod.createClassConfigurationMethods(
            objectFactory, afterClassTemplates, annotationFinder, false, xmlTest, prototypeInstance);
    ITestNGMethod[] beforeGroupsPrototypes =
        ConfigurationMethod.createBeforeConfigurationMethods(
            objectFactory, beforeGroupsTemplates, annotationFinder, true, prototypeInstance);
    List<ITestNGMethod> afterGroupsPrototypes =
        ConfigurationMethod.createAfterConfigurationMethods(
            objectFactory, afterGroupsTemplates, annotationFinder, false, prototypeInstance);
    List<ITestNGMethod> beforeMethodPrototypes =
        ConfigurationMethod.createTestMethodConfigurationMethods(
            objectFactory, beforeMethodTemplates, annotationFinder, true, xmlTest, prototypeInstance);
    List<ITestNGMethod> afterMethodPrototypes =
        ConfigurationMethod.createTestMethodConfigurationMethods(
            objectFactory, afterMethodTemplates, annotationFinder, false, xmlTest, prototypeInstance);

    for (IdentifiableObject eachInstance : instances) {
      m_beforeSuiteMethods =
          ConfigurationMethod.bind(beforeSuitePrototypes, eachInstance);
      m_afterSuiteMethods =
          ConfigurationMethod.bind(afterSuitePrototypes, eachInstance);
      m_beforeTestConfMethods =
          ConfigurationMethod.bind(beforeTestPrototypes, eachInstance);
      m_afterTestConfMethods =
          ConfigurationMethod.bind(afterTestPrototypes, eachInstance);
      m_beforeClassMethods =
          ConfigurationMethod.bind(beforeClassPrototypes, eachInstance);
      beforeClassConfig.put(eachInstance.getInstanceId(), m_beforeClassMethods);
      m_afterClassMethods =
          ConfigurationMethod.bind(afterClassPrototypes, eachInstance);
      afterClassConfig.put(eachInstance.getInstanceId(), m_afterClassMethods);
      m_beforeGroupsMethods =
          ConfigurationMethod.bind(beforeGroupsPrototypes, eachInstance);
      m_afterGroupsMethods =
          ConfigurationMethod.bind(afterGroupsPrototypes, eachInstance);
      m_beforeTestMethods.addAll(
          ConfigurationMethod.bind(beforeMethodPrototypes, eachInstance));
      m_afterTestMethods.addAll(
          ConfigurationMethod.bind(afterMethodPrototypes, eachInstance));
    }
  }

  /**
   * Create the test methods that belong to this class (rejects all those that belong to a different
   * class).
   */
  private ITestNGMethod[] createTestMethods(
      ITestNGMethod[] methods, IdentifiableObject[] instances) {
    Class<?> realClass = getRealClass();
    List<ITestNGMethod> vResult = new ArrayList<>();
    for (ITestNGMethod tm : methods) {
      ConstructorOrMethod m = tm.getConstructorOrMethod();
      if (m.getDeclaringClass().isAssignableFrom(realClass)) {
        if (instances.length == 0) {
          continue;
        }
        // Depends on the method alone, so it is not rebuilt for each @Factory instance.
        List<Pair<@Nullable XmlClass, @Nullable XmlInclude>> occurrences =
            xmlOccurrencesOf(tm.getMethodName());
        TestNGMethod prototype = null;
        int occurrence = 0;
        for (IdentifiableObject o : instances) {
          log(4, "Adding method " + tm + " on TestClass " + realClass);
          for (Pair<@Nullable XmlClass, @Nullable XmlInclude> tags : occurrences) {
            TestNGMethod method;
            if (prototype == null) {
              prototype =
                  new TestNGMethod(
                      objectFactory, m.requireMethod(), annotationFinder, xmlTest, o);
              method = prototype;
            } else {
              method = prototype.bind(o);
            }
            method.setXmlOccurrence(tags.first(), tags.second(), occurrence++);
            vResult.add(method);
          }
        }
      } else {
        log(4, "Rejecting method " + tm + " for TestClass " + realClass);
      }
    }

    return vResult.toArray(new ITestNGMethod[0]);
  }

  /**
   * The XML tags that schedule this method, one entry per run of it.
   *
   * <p>An occurrence contributes one entry per {@code <include>} naming the method exactly -- that
   * is the tag whose parameters apply, and repeating it is a request to run the method again. An
   * occurrence whose {@code <methods>} could still select it by regexp, or that lists no {@code
   * <include>} at all, contributes one entry without a tag.
   *
   * <p>A method no occurrence can select is still scheduled once, against the last of them: whether
   * it runs is {@code XmlMethodSelector}'s call, and a method that never reaches the selector is
   * never reported as excluded either. That is also the empty case -- a {@code @Factory} produced
   * class, or a suite that names none -- which falls out as a single entry carrying no tag.
   */
  private List<Pair<@Nullable XmlClass, @Nullable XmlInclude>> xmlOccurrencesOf(String methodName) {
    List<Pair<@Nullable XmlClass, @Nullable XmlInclude>> result = new ArrayList<>();
    for (XmlClass candidate : xmlClasses) {
      List<XmlInclude> includes = candidate.getIncludedMethods();
      boolean named = false;
      for (XmlInclude include : includes) {
        if (include.getName().equals(methodName)) {
          result.add(Pair.create(candidate, include));
          named = true;
        }
      }
      if (!named && (includes.isEmpty() || selects(includes, methodName))) {
        result.add(Pair.create(candidate, null));
      }
    }
    if (result.isEmpty()) {
      result.add(Pair.create(getXmlClass(), null));
    }
    return result;
  }

  /** Whether any of these {@code <include>} tags selects the method, as the selector reads them. */
  private static boolean selects(List<XmlInclude> includes, String methodName) {
    for (XmlInclude include : includes) {
      try {
        if (Pattern.compile(XmlMethodSelector.asRegexp(include.getName()))
            .matcher(methodName)
            .matches()) {
          return true;
        }
      } catch (PatternSyntaxException e) {
        // Not our error to report: XmlMethodSelector compiles the same name and warns about it.
      }
    }
    return false;
  }

  public ITestMethodFinder getTestMethodFinder() {
    return testMethodFinder;
  }

  private void log(int level, String s) {
    Utils.log("TestClass", level, s);
  }

  protected void dump() {
    LOG.info("===== Test class\n" + getRealClass().getName());
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
    return Objects.toStringHelper(getClass()).add("name", getRealClass()).toString();
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
