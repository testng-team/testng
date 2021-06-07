package org.testng.internal;

import static org.testng.internal.TestNGMethodFinder.MethodType.*;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.testng.ITestMethodFinder;
import org.testng.ITestNGMethod;
import org.testng.ITestObjectFactory;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

/** The default strategy for finding test methods: look up annotations @Test in front of methods. */
public class TestNGMethodFinder implements ITestMethodFinder {
  enum MethodType {
    BEFORE_SUITE,
    AFTER_SUITE,
    BEFORE_TEST,
    AFTER_TEST,
    BEFORE_CLASS,
    AFTER_CLASS,
    BEFORE_TEST_METHOD,
    AFTER_TEST_METHOD,
    BEFORE_GROUPS,
    AFTER_GROUPS
  }

  private static final Comparator<ITestNGMethod> NO_COMPARISON = (o1, o2) -> 0;

  private final ITestObjectFactory objectFactory;
  private final RunInfo runInfo;
  private final IAnnotationFinder annotationFinder;
  private final Comparator<ITestNGMethod> comparator;

  public TestNGMethodFinder(
      ITestObjectFactory objectFactory, RunInfo runInfo, IAnnotationFinder annotationFinder) {
    this(objectFactory, runInfo, annotationFinder, NO_COMPARISON);
  }

  public TestNGMethodFinder(
      ITestObjectFactory objectFactory,
      RunInfo runInfo,
      IAnnotationFinder annotationFinder,
      Comparator<ITestNGMethod> comparator) {
    this.objectFactory = objectFactory;
    this.runInfo = runInfo;
    this.annotationFinder = annotationFinder;
    this.comparator = comparator;
  }

  @Override
  public ITestNGMethod[] getTestMethods(Class<?> clazz, XmlTest xmlTest) {
    return AnnotationHelper.findMethodsWithAnnotation(
        objectFactory, clazz, ITestAnnotation.class, annotationFinder, xmlTest);
  }

  @Override
  public ITestNGMethod[] getBeforeClassMethods(Class<?> cls) {
    return findConfiguration(cls, BEFORE_CLASS);
  }

  @Override
  public ITestNGMethod[] getAfterClassMethods(Class<?> cls) {
    return findConfiguration(cls, AFTER_CLASS);
  }

  @Override
  public ITestNGMethod[] getBeforeTestMethods(Class<?> cls) {
    return findConfiguration(cls, BEFORE_TEST_METHOD);
  }

  @Override
  public ITestNGMethod[] getAfterTestMethods(Class<?> cls) {
    return findConfiguration(cls, AFTER_TEST_METHOD);
  }

  @Override
  public ITestNGMethod[] getBeforeSuiteMethods(Class<?> cls) {
    return findConfiguration(cls, BEFORE_SUITE);
  }

  @Override
  public ITestNGMethod[] getAfterSuiteMethods(Class<?> cls) {
    return findConfiguration(cls, AFTER_SUITE);
  }

  @Override
  public ITestNGMethod[] getBeforeTestConfigurationMethods(Class<?> clazz) {
    return findConfiguration(clazz, BEFORE_TEST);
  }

  @Override
  public ITestNGMethod[] getAfterTestConfigurationMethods(Class<?> clazz) {
    return findConfiguration(clazz, AFTER_TEST);
  }

  @Override
  public ITestNGMethod[] getBeforeGroupsConfigurationMethods(Class<?> clazz) {
    return findConfiguration(clazz, BEFORE_GROUPS);
  }

  @Override
  public ITestNGMethod[] getAfterGroupsConfigurationMethods(Class<?> clazz) {
    return findConfiguration(clazz, AFTER_GROUPS);
  }

  private ITestNGMethod[] findConfiguration(
      final Class<?> clazz, final MethodType configurationType) {
    List<ITestNGMethod> vResult = Lists.newArrayList();

    Set<Method> methods = ClassHelper.getAvailableMethodsExcludingDefaults(clazz);

    for (Method m : methods) {
      IConfigurationAnnotation configuration =
          AnnotationHelper.findConfiguration(annotationFinder, m);

      if (null == configuration) {
        continue;
      }

      boolean create;
      boolean isBeforeSuite = false;
      boolean isAfterSuite = false;
      boolean isBeforeTest = false;
      boolean isAfterTest = false;
      boolean isBeforeClass = false;
      boolean isAfterClass = false;
      boolean isBeforeTestMethod = false;
      boolean isAfterTestMethod = false;
      String[] beforeGroups = null;
      String[] afterGroups = null;

      switch (configurationType) {
        case BEFORE_SUITE:
          create = configuration.getBeforeSuite();
          isBeforeSuite = true;
          break;
        case AFTER_SUITE:
          create = configuration.getAfterSuite();
          isAfterSuite = true;
          break;
        case BEFORE_TEST:
          create = configuration.getBeforeTest();
          isBeforeTest = true;
          break;
        case AFTER_TEST:
          create = configuration.getAfterTest();
          isAfterTest = true;
          break;
        case BEFORE_CLASS:
          create = configuration.getBeforeTestClass();
          isBeforeClass = true;
          break;
        case AFTER_CLASS:
          create = configuration.getAfterTestClass();
          isAfterClass = true;
          break;
        case BEFORE_TEST_METHOD:
          create = configuration.getBeforeTestMethod();
          isBeforeTestMethod = true;
          break;
        case AFTER_TEST_METHOD:
          create = configuration.getAfterTestMethod();
          isAfterTestMethod = true;
          break;
        case BEFORE_GROUPS:
          beforeGroups = configuration.getBeforeGroups();
          create =
              shouldCreateBeforeAfterGroup(
                  beforeGroups, annotationFinder, clazz, configuration.getInheritGroups());
          isBeforeTestMethod = true;
          break;
        case AFTER_GROUPS:
          afterGroups = configuration.getAfterGroups();
          create =
              shouldCreateBeforeAfterGroup(
                  afterGroups, annotationFinder, clazz, configuration.getInheritGroups());
          isAfterTestMethod = true;
          break;
        default:
          throw new AssertionError("Unexpected value: " + configurationType);
      }

      if (create) {
        addConfigurationMethod(
            clazz,
            vResult,
            m,
            isBeforeSuite,
            isAfterSuite,
            isBeforeTest,
            isAfterTest,
            isBeforeClass,
            isAfterClass,
            isBeforeTestMethod,
            isAfterTestMethod,
            beforeGroups,
            afterGroups); /* @@@ */
      }
    }

    List<ITestNGMethod> excludedMethods = Lists.newArrayList();
    boolean unique = configurationType == BEFORE_SUITE || configurationType == AFTER_SUITE;
    return MethodHelper.collectAndOrderMethods(
        Lists.newArrayList(vResult),
        false /* forTests */,
        runInfo,
        annotationFinder,
        unique,
        excludedMethods,
        comparator);
  }

  private static boolean shouldCreateBeforeAfterGroup(
      String[] groups, IAnnotationFinder finder, Class<?> clazz, boolean isInheritGroups) {
    if (!isInheritGroups) {
      return groups.length > 0;
    }
    ITestAnnotation test = AnnotationHelper.findTest(finder, clazz);
    if (test == null) {
      return groups.length > 0;
    }
    return groups.length > 0 || test.getGroups().length > 0;
  }

  private void addConfigurationMethod(
      Class<?> clazz,
      List<ITestNGMethod> results,
      Method method,
      boolean isBeforeSuite,
      boolean isAfterSuite,
      boolean isBeforeTest,
      boolean isAfterTest,
      boolean isBeforeClass,
      boolean isAfterClass,
      boolean isBeforeTestMethod,
      boolean isAfterTestMethod,
      String[] beforeGroups,
      String[] afterGroups) {
    if (method.getDeclaringClass().isAssignableFrom(clazz)) {
      ConfigurationMethod confMethod =
          new ConfigurationMethod(
              objectFactory,
              new ConstructorOrMethod(method),
              annotationFinder,
              isBeforeSuite,
              isAfterSuite,
              isBeforeTest,
              isAfterTest,
              isBeforeClass,
              isAfterClass,
              isBeforeTestMethod,
              isAfterTestMethod,
              beforeGroups,
              afterGroups,
              this.runInfo.getXmlTest(),
              null);
      results.add(confMethod);
    }
  }
}
