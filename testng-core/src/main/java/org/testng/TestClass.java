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
 * Holds what TestNG knows about one test class in one {@code <test>}.
 *
 * <p>It holds the test methods and the configuration methods of each instance of the class. It also
 * holds the {@code <class>} tags that name the class.
 */
class TestClass extends NoOpTestClass implements ITestClass, ITestClassConfigInfo, IObject {

  private IAnnotationFinder annotationFinder;
  // Finds the test methods and the configuration methods of the class.
  private ITestMethodFinder testMethodFinder;

  private IClass iClass;
  private @Nullable String testName;
  private XmlTest xmlTest;
  // Every <class> tag that names this class, in XML order. A suite can repeat the tag. Each repeat
  // runs the methods of the class again, with its own parameters.
  private List<XmlClass> xmlClasses = Collections.emptyList();
  private final ITestObjectFactory objectFactory;
  private final @Nullable String m_errorMsgPrefix;

  // Keyed by the instance id, not by the instance. Reading the instance would make a lazy @Factory
  // create it while TestNG collects the methods.
  private final Map<UUID, List<ITestNGMethod>> beforeClassConfig = new LinkedHashMap<>();

  private final Map<UUID, List<ITestNGMethod>> afterClassConfig = new LinkedHashMap<>();

  // Keyed by the instance id too. Each test method call looks up its own list here. A scan of the
  // full @BeforeMethod and @AfterMethod lists on each call would be slow for a large @Factory.
  private final Map<UUID, List<ITestNGMethod>> beforeMethodConfig = new LinkedHashMap<>();

  private final Map<UUID, List<ITestNGMethod>> afterMethodConfig = new LinkedHashMap<>();

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

  @Override
  public List<ITestNGMethod> getInstanceBeforeTestMethods(@Nullable UUID instanceId) {
    return beforeMethodConfig.getOrDefault(instanceId, Collections.emptyList());
  }

  @Override
  public List<ITestNGMethod> getInstanceAfterTestMethods(@Nullable UUID instanceId) {
    return afterMethodConfig.getOrDefault(instanceId, Collections.emptyList());
  }

  private static final Logger LOG = Logger.getLogger(TestClass.class);

  /**
   * Creates the test class, its instances and its test methods.
   *
   * @param objectFactory the factory that TestNG uses to create objects.
   * @param cls the class and its instances.
   * @param testMethodFinder finds the test methods and the configuration methods of the class.
   * @param annotationFinder reads the TestNG annotations of the class.
   * @param xmlTest the {@code <test>} that runs the class.
   * @param xmlClasses the {@code <class>} tags that name the class, in XML order. The list is empty
   *     when no tag names the class.
   * @param errorMsgPrefix text that TestNG puts in front of the error when it cannot create an
   *     instance, or {@code null}.
   */
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
    // This method can return one tag only, so it returns the last one, as ClassInfoMap does.
    return xmlClasses.isEmpty() ? null : xmlClasses.get(xmlClasses.size() - 1);
  }

  /**
   * Returns the finder that reads the TestNG annotations of the class.
   *
   * @return the annotation finder.
   */
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
    initTestMethods();
  }

  private void initTestClassesAndInstances() {
    //
    // Get the instances, and take the test name from the first one that implements ITest
    //
    IObject.IdentifiableObject[] instances = getObjects(true, this.m_errorMsgPrefix);
    Arrays.stream(instances)
        .map(IdentifiableObject::getInstance)
        // Look only at instances that exist. Do not create a lazy @Factory instance just to read
        // its ITest name. When no instance gives a name, the test name of the class applies.
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

  private void initTestMethods() {
    Class<?> realClass = getRealClass();
    ITestNGMethod[] methods = testMethodFinder.getTestMethods(realClass, xmlTest);
    IdentifiableObject[] instances = IObject.objects(iClass, false);
    m_testMethods = createTestMethods(methods, instances);
  }

  /**
   * Creates the configuration methods of each instance of the class.
   *
   * <p>TestNG filters configuration methods with {@link IMethodSelector#includeMethod}. A selector
   * can answer only after {@link IMethodSelector#setTestMethods} gives it the test methods. So call
   * this method after that.
   */
  void initConfigurationMethods() {
    IdentifiableObject[] instances = IObject.objects(iClass, false);
    if (instances.length == 0) {
      return;
    }

    Class<?> realClass = getRealClass();
    // Each lookup scans the whole class hierarchy, and none depends on the instance. So look up
    // each kind of configuration method once, then bind the result to each instance.
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

    for (IdentifiableObject eachInstance : instances) {
      m_beforeSuiteMethods =
          ConfigurationMethod.createSuiteConfigurationMethods(
              objectFactory, beforeSuiteTemplates, annotationFinder, true, eachInstance);
      m_afterSuiteMethods =
          ConfigurationMethod.createSuiteConfigurationMethods(
              objectFactory, afterSuiteTemplates, annotationFinder, false, eachInstance);
      m_beforeTestConfMethods =
          ConfigurationMethod.createTestConfigurationMethods(
              objectFactory,
              beforeTestTemplates,
              annotationFinder,
              true,
              this.xmlTest,
              eachInstance);
      m_afterTestConfMethods =
          ConfigurationMethod.createTestConfigurationMethods(
              objectFactory,
              afterTestTemplates,
              annotationFinder,
              false,
              this.xmlTest,
              eachInstance);
      m_beforeClassMethods =
          ConfigurationMethod.createClassConfigurationMethods(
              objectFactory, beforeClassTemplates, annotationFinder, true, xmlTest, eachInstance);
      beforeClassConfig.put(eachInstance.getInstanceId(), m_beforeClassMethods);
      m_afterClassMethods =
          ConfigurationMethod.createClassConfigurationMethods(
              objectFactory, afterClassTemplates, annotationFinder, false, xmlTest, eachInstance);
      afterClassConfig.put(eachInstance.getInstanceId(), m_afterClassMethods);
      m_beforeGroupsMethods =
          ConfigurationMethod.createBeforeConfigurationMethods(
              objectFactory, beforeGroupsTemplates, annotationFinder, true, eachInstance);
      m_afterGroupsMethods =
          ConfigurationMethod.createAfterConfigurationMethods(
              objectFactory, afterGroupsTemplates, annotationFinder, false, eachInstance);
      List<ITestNGMethod> beforeMethods =
          ConfigurationMethod.createTestMethodConfigurationMethods(
              objectFactory, beforeMethodTemplates, annotationFinder, true, xmlTest, eachInstance);
      m_beforeTestMethods.addAll(beforeMethods);
      beforeMethodConfig.put(eachInstance.getInstanceId(), beforeMethods);
      List<ITestNGMethod> afterMethods =
          ConfigurationMethod.createTestMethodConfigurationMethods(
              objectFactory, afterMethodTemplates, annotationFinder, false, xmlTest, eachInstance);
      m_afterTestMethods.addAll(afterMethods);
      afterMethodConfig.put(eachInstance.getInstanceId(), afterMethods);
    }
  }

  /**
   * Creates the test methods of this class, for each instance and each XML occurrence.
   *
   * <p>It skips a method when the class that declares the method is not this class or a parent of
   * it.
   */
  private ITestNGMethod[] createTestMethods(
      ITestNGMethod[] methods, IdentifiableObject[] instances) {
    Class<?> realClass = getRealClass();
    List<ITestNGMethod> vResult = new ArrayList<>();
    for (ITestNGMethod tm : methods) {
      ConstructorOrMethod m = tm.getConstructorOrMethod();
      if (m.getDeclaringClass().isAssignableFrom(realClass)) {
        // This list depends on the method only, so build it once, not once for each instance.
        List<Pair<@Nullable XmlClass, @Nullable XmlInclude>> occurrences =
            xmlOccurrencesOf(tm.getMethodName());
        for (IdentifiableObject o : instances) {
          log(4, "Adding method " + tm + " on TestClass " + realClass);
          int occurrence = 0;
          for (Pair<@Nullable XmlClass, @Nullable XmlInclude> tags : occurrences) {
            TestNGMethod created =
                new TestNGMethod(objectFactory, m.requireMethod(), annotationFinder, xmlTest, o);
            created.setXmlOccurrence(tags.first(), tags.second(), occurrence++);
            vResult.add(created);
          }
        }
      } else {
        log(4, "Rejecting method " + tm + " for TestClass " + realClass);
      }
    }

    return vResult.toArray(new ITestNGMethod[0]);
  }

  /**
   * Returns the XML tags that schedule a method, with one entry for each run of the method.
   *
   * <p>Each {@code <class>} tag of this class gives entries this way:
   *
   * <ul>
   *   <li>Each {@code <include>} with the exact name of the method gives one entry. The parameters
   *       of that {@code <include>} apply to that run. A repeated {@code <include>} runs the method
   *       again.
   *   <li>With no exact {@code <include>}, the tag gives one entry without an {@code <include>}.
   *       This happens when the tag has no {@code <include>} at all, or when an {@code <include>}
   *       selects the method by a regular expression.
   * </ul>
   *
   * <p>When no tag selects the method, the method still gets one entry, with the last {@code
   * <class>} tag. The method must reach {@code XmlMethodSelector}, which decides if it runs. A
   * method that never reaches the selector is not reported as excluded. The same rule covers a
   * class with no {@code <class>} tag, such as a class that a {@code @Factory} made. It gets one
   * entry with no tags.
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

  /**
   * Tells if one of the {@code <include>} tags selects the method, the same way that {@code
   * XmlMethodSelector} reads them.
   */
  private static boolean selects(List<XmlInclude> includes, String methodName) {
    for (XmlInclude include : includes) {
      try {
        if (Pattern.compile(XmlMethodSelector.asRegexp(include.getName()))
            .matcher(methodName)
            .matches()) {
          return true;
        }
      } catch (PatternSyntaxException e) {
        // Do not report the bad pattern here. XmlMethodSelector compiles the same name and warns
        // about it.
      }
    }
    return false;
  }

  /**
   * Returns the finder of the test methods and the configuration methods of the class.
   *
   * @return the test method finder.
   */
  public ITestMethodFinder getTestMethodFinder() {
    return testMethodFinder;
  }

  private void log(int level, String s) {
    Utils.log("TestClass", level, s);
  }

  /**
   * Logs the {@code @BeforeClass}, {@code @BeforeMethod}, {@code @Test}, {@code @AfterMethod} and
   * {@code @AfterClass} methods of this class, for debugging.
   */
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

  /**
   * Returns the {@link IClass} that holds the class and its instances.
   *
   * @return the class.
   */
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
