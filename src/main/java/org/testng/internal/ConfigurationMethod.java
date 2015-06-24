package org.testng.internal;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.testng.ITestNGMethod;
import org.testng.annotations.IAnnotation;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.collections.Lists;
import org.testng.collections.Maps;
import org.testng.internal.annotations.AnnotationHelper;
import org.testng.internal.annotations.ConfigurationAnnotation;
import org.testng.internal.annotations.IAfterClass;
import org.testng.internal.annotations.IAfterGroups;
import org.testng.internal.annotations.IAfterMethod;
import org.testng.internal.annotations.IAfterSuite;
import org.testng.internal.annotations.IAfterTest;
import org.testng.internal.annotations.IAnnotationFinder;
import org.testng.internal.annotations.IBeforeClass;
import org.testng.internal.annotations.IBeforeGroups;
import org.testng.internal.annotations.IBeforeMethod;
import org.testng.internal.annotations.IBeforeSuite;
import org.testng.internal.annotations.IBeforeTest;

public class ConfigurationMethod extends BaseTestMethod {
  /**
   *
   */
  private static final long serialVersionUID = -6537771498553619645L;
  private final boolean m_isBeforeSuiteConfiguration;
  private final boolean m_isAfterSuiteConfiguration;

  private final boolean m_isBeforeTestConfiguration;
  private final boolean m_isAfterTestConfiguration;

  private final boolean m_isBeforeClassConfiguration;
  private final boolean m_isAfterClassConfiguration;

  private final boolean m_isBeforeMethodConfiguration;
  private final boolean m_isAfterMethodConfiguration;

  private boolean m_inheritGroupsFromTestClass = false;

  private ConfigurationMethod(ConstructorOrMethod com,
                              IAnnotationFinder annotationFinder,
                              boolean isBeforeSuite,
                              boolean isAfterSuite,
                              boolean isBeforeTest,
                              boolean isAfterTest,
                              boolean isBeforeClass,
                              boolean isAfterClass,
                              boolean isBeforeMethod,
                              boolean isAfterMethod,
                              String[] beforeGroups,
                              String[] afterGroups,
                              boolean initialize,
                              Object instance)
  {
    super(com.getName(), com, annotationFinder, instance);
    if(initialize) {
      init();
    }

    m_isBeforeSuiteConfiguration = isBeforeSuite;
    m_isAfterSuiteConfiguration = isAfterSuite;

    m_isBeforeTestConfiguration = isBeforeTest;
    m_isAfterTestConfiguration = isAfterTest;

    m_isBeforeClassConfiguration = isBeforeClass;
    m_isAfterClassConfiguration = isAfterClass;

    m_isBeforeMethodConfiguration = isBeforeMethod;
    m_isAfterMethodConfiguration = isAfterMethod;

    m_beforeGroups = beforeGroups;
    m_afterGroups = afterGroups;

  }

  /**
   * @deprecated use #ConfigurationMethod(ConstructorOrMethod,...) instead.
   */
  @Deprecated
  public ConfigurationMethod(Method method,
                             IAnnotationFinder annotationFinder,
                             boolean isBeforeSuite,
                             boolean isAfterSuite,
                             boolean isBeforeTest,
                             boolean isAfterTest,
                             boolean isBeforeClass,
                             boolean isAfterClass,
                             boolean isBeforeMethod,
                             boolean isAfterMethod,
                             String[] beforeGroups,
                             String[] afterGroups,
                             Object instance)
  {
    this(new ConstructorOrMethod(method), annotationFinder, isBeforeSuite, isAfterSuite, isBeforeTest, isAfterTest,
        isBeforeClass, isAfterClass, isBeforeMethod, isAfterMethod, beforeGroups, afterGroups, instance);
  }

  public ConfigurationMethod(ConstructorOrMethod com,
                             IAnnotationFinder annotationFinder,
                             boolean isBeforeSuite,
                             boolean isAfterSuite,
                             boolean isBeforeTest,
                             boolean isAfterTest,
                             boolean isBeforeClass,
                             boolean isAfterClass,
                             boolean isBeforeMethod,
                             boolean isAfterMethod,
                             String[] beforeGroups,
                             String[] afterGroups,
                             Object instance) {
    this(com, annotationFinder, isBeforeSuite, isAfterSuite, isBeforeTest, isAfterTest,
        isBeforeClass, isAfterClass, isBeforeMethod, isAfterMethod, beforeGroups, afterGroups,
        true, instance);
  }

  private static ITestNGMethod[] createMethods(ITestNGMethod[] methods, IAnnotationFinder finder,
      boolean isBeforeSuite,
      boolean isAfterSuite,
      boolean isBeforeTest,
      boolean isAfterTest,
      boolean isBeforeClass,
      boolean isAfterClass,
      boolean isBeforeMethod,
      boolean isAfterMethod,
      String[] beforeGroups,
      String[] afterGroups,
      Object instance)
  {
    List<ITestNGMethod> result = Lists.newArrayList();
      for (ITestNGMethod method : methods) {
          result.add(new ConfigurationMethod(method.getConstructorOrMethod(),
                  finder,
                  isBeforeSuite,
                  isAfterSuite,
                  isBeforeTest,
                  isAfterTest,
                  isBeforeClass,
                  isAfterClass,
                  isBeforeMethod,
                  isAfterMethod,
                  new String[0],
                  new String[0],
                  instance));
      }

    return result.toArray(new ITestNGMethod[result.size()]);
  }


  public static ITestNGMethod[] createSuiteConfigurationMethods(ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder, boolean isBefore, Object instance) {
    return createMethods(methods, annotationFinder,
        isBefore,
        !isBefore,
        false,
        false,
        false,
        false,
        false,
        false,
        new String[0],
        new String[0],
        instance);
  }

  public static ITestNGMethod[] createTestConfigurationMethods(ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder, boolean isBefore, Object instance) {
    return createMethods(methods, annotationFinder,
        false,
        false,
        isBefore,
        !isBefore,
        false,
        false,
        false,
        false,
        new String[0],
        new String[0],
        instance);
  }

  public static ITestNGMethod[] createClassConfigurationMethods(ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder, boolean isBefore, Object instance)  {
    return createMethods(methods, annotationFinder,
        false,
        false,
        false,
        false,
        isBefore,
        !isBefore,
        false,
        false,
        new String[0],
        new String[0],
        instance);
  }

  public static ITestNGMethod[] createBeforeConfigurationMethods(ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder, boolean isBefore, Object instance)
  {
    ITestNGMethod[] result = new ITestNGMethod[methods.length];
    for(int i = 0; i < methods.length; i++) {
      result[i] = new ConfigurationMethod(methods[i].getConstructorOrMethod(),
          annotationFinder,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          isBefore ? methods[i].getBeforeGroups() : new String[0],
          new String[0],
          instance);
      }

    return result;
  }

  public static ITestNGMethod[] createAfterConfigurationMethods(ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder, boolean isBefore, Object instance)
  {
    ITestNGMethod[] result = new ITestNGMethod[methods.length];
    for(int i = 0; i < methods.length; i++) {
      result[i] = new ConfigurationMethod(methods[i].getConstructorOrMethod(),
          annotationFinder,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          new String[0],
          isBefore ? new String[0] : methods[i].getAfterGroups(),
          instance);
      }

    return result;
  }

  public static ITestNGMethod[] createTestMethodConfigurationMethods(ITestNGMethod[] methods,
      IAnnotationFinder annotationFinder, boolean isBefore, Object instance) {
    return createMethods(methods, annotationFinder,
        false,
        false,
        false,
        false,
        false,
        false,
        isBefore,
        !isBefore,
        new String[0],
        new String[0],
        instance);
  }

  /**
   * @return Returns the isAfterClassConfiguration.
   */
  @Override
  public boolean isAfterClassConfiguration() {
    return m_isAfterClassConfiguration;
  }
  /**
   * @return Returns the isAfterMethodConfiguration.
   */
  @Override
  public boolean isAfterMethodConfiguration() {
    return m_isAfterMethodConfiguration;
  }
  /**
   * @return Returns the isBeforeClassConfiguration.
   */
  @Override
  public boolean isBeforeClassConfiguration() {
    return m_isBeforeClassConfiguration;
  }
  /**
   * @return Returns the isBeforeMethodConfiguration.
   */
  @Override
  public boolean isBeforeMethodConfiguration() {
    return m_isBeforeMethodConfiguration;
  }


  /**
   * @return Returns the isAfterSuiteConfiguration.
   */
  @Override
  public boolean isAfterSuiteConfiguration() {
    return m_isAfterSuiteConfiguration;
  }

  /**
   * @return Returns the isBeforeSuiteConfiguration.
   */
  @Override
  public boolean isBeforeSuiteConfiguration() {
    return m_isBeforeSuiteConfiguration;
  }

  @Override
  public boolean isBeforeTestConfiguration() {
    return m_isBeforeTestConfiguration;
  }

  @Override
  public boolean isAfterTestConfiguration() {
    return m_isAfterTestConfiguration;
  }

  @Override
  public boolean isBeforeGroupsConfiguration() {
    return m_beforeGroups != null && m_beforeGroups.length > 0;
  }

  @Override
  public boolean isAfterGroupsConfiguration() {
    return m_afterGroups != null && m_afterGroups.length > 0;
  }

  private boolean inheritGroupsFromTestClass() {
    return m_inheritGroupsFromTestClass;
  }

  private void init() {
    IAnnotation a = AnnotationHelper.findConfiguration(m_annotationFinder, m_method.getMethod());
    IConfigurationAnnotation annotation = (IConfigurationAnnotation) a;
    if (a != null) {
      m_inheritGroupsFromTestClass = annotation.getInheritGroups();
      setEnabled(annotation.getEnabled());
      setDescription(annotation.getDescription());
    }

    if (annotation != null && annotation.isFakeConfiguration()) {
     if (annotation.getBeforeSuite()) {
      initGroups(IBeforeSuite.class);
    }
     if (annotation.getAfterSuite()) {
      initGroups(IAfterSuite.class);
    }
     if (annotation.getBeforeTest()) {
      initGroups(IBeforeTest.class);
    }
     if (annotation.getAfterTest()) {
      initGroups(IAfterTest.class);
    }
     if (annotation.getBeforeGroups().length != 0) {
      initGroups(IBeforeGroups.class);
    }
     if (annotation.getAfterGroups().length != 0) {
      initGroups(IAfterGroups.class);
    }
     if (annotation.getBeforeTestClass()) {
      initGroups(IBeforeClass.class);
    }
     if (annotation.getAfterTestClass()) {
      initGroups(IAfterClass.class);
    }
     if (annotation.getBeforeTestMethod()) {
      initGroups(IBeforeMethod.class);
    }
     if (annotation.getAfterTestMethod()) {
      initGroups(IAfterMethod.class);
    }
    }
    else {
      initGroups(IConfigurationAnnotation.class);
    }

    // If this configuration method has inherit-groups=true, add the groups
    // defined in the @Test class
    if (inheritGroupsFromTestClass()) {
      ITestAnnotation classAnnotation = m_annotationFinder.findAnnotation(m_methodClass, ITestAnnotation.class);
      if (classAnnotation != null) {
        String[] groups = classAnnotation.getGroups();
        Map<String, String> newGroups = Maps.newHashMap();
        for (String g : getGroups()) {
          newGroups.put(g, g);
        }
        if (groups != null) {
          for (String g : groups) {
            newGroups.put(g, g);
          }
          setGroups(newGroups.values().toArray(new String[newGroups.size()]));
        }
      }
    }

    if (annotation != null) {
      setTimeOut(annotation.getTimeOut());
    }
  }

  private static void ppp(String s) {
    System.out.println("[ConfigurationMethod] " + s);
  }

  @Override
  public ConfigurationMethod clone() {
    ConfigurationMethod clone= new ConfigurationMethod(getConstructorOrMethod(),
        getAnnotationFinder(),
        isBeforeSuiteConfiguration(),
        isAfterSuiteConfiguration(),
        isBeforeTestConfiguration(),
        isAfterTestConfiguration(),
        isBeforeClassConfiguration(),
        isAfterClassConfiguration(),
        isBeforeMethodConfiguration(),
        isAfterMethodConfiguration(),
        getBeforeGroups(),
        getAfterGroups(),
        false /* do not call init() */,
        getInstance()
        );
    clone.m_testClass= getTestClass();
    clone.setDate(getDate());
    clone.setGroups(getGroups());
    clone.setGroupsDependedUpon(getGroupsDependedUpon(), Collections.<String>emptyList());
    clone.setMethodsDependedUpon(getMethodsDependedUpon());
    clone.setAlwaysRun(isAlwaysRun());
    clone.setMissingGroup(getMissingGroup());
    clone.setDescription(getDescription());
    clone.setEnabled(getEnabled());
    clone.setParameterInvocationCount(getParameterInvocationCount());
    clone.m_inheritGroupsFromTestClass= inheritGroupsFromTestClass();

    return clone;
  }

  public boolean isFirstTimeOnly() {
    boolean result = false;
    IAnnotation before = m_annotationFinder.findAnnotation(getMethod(), IBeforeMethod.class);
    if (before != null) {
      result = ((ConfigurationAnnotation) before).isFirstTimeOnly();
    }
    return result;
  }

  public boolean isLastTimeOnly() {
    boolean result = false;
    IAnnotation before = m_annotationFinder.findAnnotation(getMethod(), IAfterMethod.class);
    if (before != null) {
      result = ((ConfigurationAnnotation) before).isLastTimeOnly();
    }
    return result;
  }

}

