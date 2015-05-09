package org.testng.internal;


import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.testng.ITestMethodFinder;
import org.testng.ITestNGMethod;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.xml.XmlTest;

/**
 * The default strategy for finding test methods:  look up
 * annotations @Test in front of methods.
 *
 * @author Cedric Beust, May 3, 2004
 * @author <a href='mailto:the_mindstorm@evolva.ro'>Alexandru Popescu</a>
 */
public class TestNGMethodFinder implements ITestMethodFinder {
  private static final int BEFORE_SUITE = 1;
  private static final int AFTER_SUITE = 2;
  private static final int BEFORE_TEST = 3;
  private static final int AFTER_TEST = 4;
  private static final int BEFORE_CLASS = 5;
  private static final int AFTER_CLASS = 6;
  private static final int BEFORE_TEST_METHOD = 7;
  private static final int AFTER_TEST_METHOD = 8;
  private static final int BEFORE_GROUPS = 9;
  private static final int AFTER_GROUPS = 10;

  private RunInfo m_runInfo = null;
  private IAnnotationFinder m_annotationFinder = null;

  public TestNGMethodFinder(RunInfo runInfo, IAnnotationFinder annotationFinder)
  {
    m_runInfo = runInfo;
    m_annotationFinder = annotationFinder;
  }

  @Override
  public ITestNGMethod[] getTestMethods(Class<?> clazz, XmlTest xmlTest) {
    return AnnotationHelper.findMethodsWithAnnotation(
        clazz, ITestAnnotation.class, m_annotationFinder, xmlTest);
  }

  @Override
  public ITestNGMethod[] getBeforeClassMethods(Class cls) {
    return findConfiguration(cls, BEFORE_CLASS);
  }

  @Override
  public ITestNGMethod[] getAfterClassMethods(Class cls) {
    return findConfiguration(cls, AFTER_CLASS);
  }

  @Override
  public ITestNGMethod[] getBeforeTestMethods(Class cls) {
    return findConfiguration(cls, BEFORE_TEST_METHOD);
  }

  @Override
  public ITestNGMethod[] getAfterTestMethods(Class cls) {
    return findConfiguration(cls, AFTER_TEST_METHOD);
  }

  @Override
  public ITestNGMethod[] getBeforeSuiteMethods(Class cls) {
    return findConfiguration(cls, BEFORE_SUITE);
  }

  @Override
  public ITestNGMethod[] getAfterSuiteMethods(Class cls) {
    return findConfiguration(cls, AFTER_SUITE);
  }

  @Override
  public ITestNGMethod[] getBeforeTestConfigurationMethods(Class clazz) {
    return findConfiguration(clazz, BEFORE_TEST);
  }

  @Override
  public ITestNGMethod[] getAfterTestConfigurationMethods(Class clazz) {
    return findConfiguration(clazz, AFTER_TEST);
  }

  @Override
  public ITestNGMethod[] getBeforeGroupsConfigurationMethods(Class clazz) {
    return findConfiguration(clazz, BEFORE_GROUPS);
  }

  @Override
  public ITestNGMethod[] getAfterGroupsConfigurationMethods(Class clazz) {
    return findConfiguration(clazz, AFTER_GROUPS);
  }

  private ITestNGMethod[] findConfiguration(final Class clazz, final int configurationType) {
    List<ITestNGMethod> vResult = Lists.newArrayList();

    Set<Method> methods = ClassHelper.getAvailableMethods(clazz);

    for (Method m : methods) {
      IConfigurationAnnotation configuration = AnnotationHelper.findConfiguration(m_annotationFinder, m);

      if (null == configuration) {
        continue;
      }

      boolean create = false;
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

      switch(configurationType) {
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
          create = beforeGroups.length > 0;
          isBeforeTestMethod = true;
          break;
        case AFTER_GROUPS:
          afterGroups = configuration.getAfterGroups();
          create = afterGroups.length > 0;
          isBeforeTestMethod = true;
          break;
      }

      if(create) {
        addConfigurationMethod(clazz,
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
                               afterGroups,
                               null); /* @@@ */
      }
    }

    List<ITestNGMethod> excludedMethods = Lists.newArrayList();
    boolean unique = configurationType == BEFORE_SUITE || configurationType == AFTER_SUITE;
    ITestNGMethod[] tmResult = MethodHelper.collectAndOrderMethods(Lists.newArrayList(vResult),
                                              false /* forTests */,
                                              m_runInfo,
                                              m_annotationFinder,
                                              unique,
                                              excludedMethods);

    return tmResult;
  }

  private void addConfigurationMethod(Class<?> clazz,
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
                                      String[] afterGroups,
                                      Object instance)
  {
    if(method.getDeclaringClass().isAssignableFrom(clazz)) {
      ITestNGMethod confMethod = new ConfigurationMethod(new ConstructorOrMethod(method),
                                                         m_annotationFinder,
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
                                                         instance);
      results.add(confMethod);
    }
  }

}
